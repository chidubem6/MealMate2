# PRD: MealMate — Native Android Meal Planning App

## 1. Product overview

**App name:** MealMate  
**Platform:** Native Android  
**Language:** Kotlin  
**Development environment:** Android Studio  
**Coursework:** 25COB155 Mobile Application Development  
**Development approach:** Individual coursework project built from scratch using Kotlin and native Android APIs.

MealMate is a local-first Android meal planning app that helps users create their own meals, choose how many meals they want to plan per day, assign meals to a weekly plan, automatically generate a combined shopping list, share the shopping list, and receive reminders to shop or prepare meals.

The app is designed to satisfy the 25COB155 coursework specification while staying realistic to build within the coursework timeframe.

---

## 2. Confirmed product decisions

The following decisions have been confirmed:

1. The user can choose how many meals they want to plan per day.
2. Duplicate ingredients should be combined in the shopping list.
3. Shopping list totals should be calculated where ingredient names and units match.
4. The app will be local-only.
5. Firebase will not be used.
6. The selected optional features are:
   - Android ShareSheet,
   - Notifications,
   - adaptation for different screen sizes.
7. The target audience is general meal planners, not only students.
8. Users will create their own meals.

---

## 3. Problem statement

Many people struggle to plan meals consistently, forget ingredients while shopping, or buy duplicate items because they do not have a clear weekly plan.

MealMate solves this by allowing users to create meals, plan meals across the week, and automatically generate a shopping list from the selected meals. The shopping list combines duplicate ingredients and calculates totals, making shopping clearer and reducing waste.

---

## 4. Target users

### Primary target user

General meal planners who want a simple way to organise meals and shopping without using a complex nutrition, calorie, or recipe app.

### Example users

- Someone planning meals for the week.
- Someone shopping for a household.
- Someone trying to reduce food waste.
- Someone who wants a simple grocery list generated from planned meals.

---

## 5. Product goals

### Functional goals

1. Allow users to create their own meals.
2. Allow users to add ingredients with quantity and unit.
3. Allow users to choose how many meals they want to plan per day.
4. Allow users to assign meals to meal slots across the week.
5. Automatically generate a shopping list from the weekly plan.
6. Combine duplicate ingredients in the shopping list where possible.
7. Calculate total quantities for matching ingredients and units.
8. Allow users to manually add extra shopping items.
9. Allow users to check off shopping items.
10. Allow users to share the shopping list through Android ShareSheet.
11. Allow users to enable shopping or meal-prep reminders.

### Coursework goals

1. Build a native Android app using Kotlin.
2. Use the Navigation Component to move between screens.
3. Use an external Intent through Android ShareSheet.
4. Use local storage.
5. Handle lifecycle changes such as screen rotation.
6. Use permissions responsibly.
7. Create a custom ContentProvider.
8. Write instrumented tests for the ContentProvider.
9. Implement exactly three optional features:
   - ShareSheet,
   - Notifications,
   - different screen sizes.

---

## 6. Non-goals

MealMate will not include the following in the coursework version:

- Firebase,
- cloud sync,
- login,
- user accounts,
- calorie tracking,
- macro tracking,
- barcode scanning,
- payment features,
- AI meal generation,
- social feeds,
- delivery or ordering features.

The app is deliberately local-only so the implementation stays focused on Android coursework requirements rather than backend development.

---

## 7. Coursework requirement mapping

| Coursework requirement | MealMate implementation |
|---|---|
| Native Android app | Built in Android Studio using Kotlin |
| Minimum two screens | App includes Meal List, Create Meal, Meal Detail, Weekly Plan, Shopping List, and Settings screens |
| Navigation Component | Used for internal screen navigation |
| Intent to outside app | Share shopping list via Android ShareSheet |
| Lifecycle handling | ViewModel and SavedStateHandle preserve UI state on rotation |
| Responsible permissions | Notification permission requested only when reminders are enabled |
| Local storage | Room database and SharedPreferences |
| Custom ContentProvider | `MealMateContentProvider` exposes meal and shopping data |
| Instrumented tests | CRUD tests for ContentProvider |
| Optional feature 1 | Android ShareSheet |
| Optional feature 2 | Notifications |
| Optional feature 3 | Adaptation for different screen sizes |
| Code quality | Clear architecture, readable Kotlin, separation of concerns |
| Demo | Clear proposal, design, implementation overview, and security reflection |

---

## 8. Selected optional features

## 8.1 Android ShareSheet

Users can share the generated shopping list as plain text using Android ShareSheet.

Example shared output:

```text
MealMate Shopping List

Beef Mince — 1000g
Tinned Tomatoes — 800g
Garlic Cloves — 4
Spaghetti — 400g
Chicken Breast — 2
```

This satisfies the coursework requirement to use an Intent to move to an outside app.

---

## 8.2 Notifications

Users can enable reminders for:

- weekly shopping,
- meal preparation.

On Android 13 and above, the app will request the `POST_NOTIFICATIONS` permission only when the user enables reminders.

---

## 8.3 Adaptation for different screen sizes

MealMate will support:

- portrait phone layouts,
- landscape phone layouts,
- larger screens where practical.

Implementation approach:

- use responsive XML layouts,
- avoid hardcoded widths and heights,
- use `ScrollView` for long content,
- use ConstraintLayout where suitable,
- provide alternative layout resources such as `layout-land` or `layout-w600dp` if needed.

---

## 9. Meal slots and daily meal count

Unlike a fixed breakfast/lunch/dinner system, MealMate allows the user to choose how many meals they want to plan per day.

### User setting

The user can choose a default daily meal count, for example:

- 1 meal per day,
- 2 meals per day,
- 3 meals per day,
- custom number.

### Example

If the user chooses **3 meals per day**, the weekly plan displays:

```text
Monday
- Meal 1
- Meal 2
- Meal 3

Tuesday
- Meal 1
- Meal 2
- Meal 3
```

The app may optionally allow users to rename slots later, for example:

- Breakfast,
- Lunch,
- Dinner,
- Snack,
- Pre-gym meal.

For the coursework MVP, the safer implementation is to use simple labels such as `Meal 1`, `Meal 2`, and `Meal 3`.

---

## 10. Ingredient combining logic

The shopping list should combine duplicate ingredients where the ingredient name and unit match.

### Example input

```text
Meal: Spaghetti Bolognese
- Beef Mince, 500, g
- Tinned Tomatoes, 400, g
- Onion, 1, item

Meal: Homemade Burger
- Beef Mince, 500, g
- Onion, 1, item
```

### Generated shopping list

```text
Beef Mince — 1000g
Tinned Tomatoes — 400g
Onion — 2 items
```

### Combining rule

Ingredients can be combined when:

1. ingredient names match after trimming spaces and normalising case,
2. units match,
3. quantities are numeric.

### If quantities cannot be combined

If quantities are not numeric or units do not match, the app should list them separately.

Example:

```text
Milk — 500ml
Milk — 1 carton
```

This keeps the implementation realistic and prevents incorrect totals.

---

## 11. Core features

## Feature 1: Meal list

### Description

Users can view all meals they have created.

### Functional requirements

The app shall allow users to:

- view saved meals,
- open meal details,
- create a new meal,
- edit a meal,
- delete a meal.

### Main UI elements

- app title,
- meal cards,
- Add Meal button,
- bottom navigation.

---

## Feature 2: Create meal

### Description

Users can create their own meals and add ingredients.

### Functional requirements

The app shall allow users to:

- enter a meal name,
- enter ingredient name,
- enter ingredient quantity,
- select or type ingredient unit,
- add multiple ingredients,
- preview ingredients before saving,
- save the meal locally.

### Main UI elements

- meal name input,
- ingredient name input,
- quantity input,
- unit input,
- Add Ingredient button,
- ingredient preview list,
- Save Meal button.

---

## Feature 3: Meal detail

### Description

Users can view a saved meal and its ingredients.

### Functional requirements

The app shall allow users to:

- view meal name,
- view ingredient list,
- edit the meal,
- delete the meal,
- return to the meal list.

### Main UI elements

- meal title,
- ingredient list,
- Edit button,
- Delete button.

---

## Feature 4: Weekly plan

### Description

Users can assign meals to meal slots across the week.

### Functional requirements

The app shall allow users to:

- select how many meals they want per day,
- view a weekly plan from Monday to Sunday,
- assign a saved meal to each meal slot,
- change an assigned meal,
- remove an assigned meal,
- generate a shopping list from the full plan.

### Main UI elements

- daily meal count selector,
- Monday to Sunday list,
- meal slots under each day,
- Assign Meal buttons,
- Generate Shopping List button.

---

## Feature 5: Shopping list

### Description

The app generates a shopping list from the weekly plan.

### Functional requirements

The app shall:

- retrieve ingredients from all planned meals,
- combine duplicate ingredients where possible,
- calculate total quantities,
- display ingredient name, total quantity, and unit,
- allow manual shopping items,
- allow users to check off items,
- allow users to share the list.

### Main UI elements

- shopping item checklist,
- total quantity display,
- Add Item button,
- Share List button,
- Clear Checked Items button.

---

## Feature 6: Settings and reminders

### Description

Users can manage reminder preferences and daily meal count.

### Functional requirements

The app shall allow users to:

- set default number of meals per day,
- enable or disable shopping reminders,
- select reminder day and time,
- enable or disable meal-prep reminders,
- request notification permission only when needed,
- save preferences locally.

### Main UI elements

- daily meal count selector,
- reminder toggles,
- day/time picker,
- permission explanation text,
- Save Settings button.

---

## 12. Screens

MealMate will include the following screens:

1. **Meal List Screen**
2. **Create Meal Screen**
3. **Meal Detail Screen**
4. **Weekly Plan Screen**
5. **Shopping List Screen**
6. **Settings / Reminders Screen**

This exceeds the minimum two-screen coursework requirement while remaining achievable.

---

## 13. Navigation flow

```text
Meal List
 ├── Create Meal
 ├── Meal Detail
 ├── Weekly Plan
 │    ├── Set daily meal count
 │    ├── Assign Meal to Slot
 │    └── Generate Shopping List → Shopping List
 ├── Shopping List
 │    └── Share List → Android ShareSheet
 └── Settings / Reminders
      ├── Set daily meal count
      └── Enable notifications
```

Internal movement will use the Navigation Component.

External movement will use Android ShareSheet.

---

## 14. Data model

## 14.1 Meal

| Field | Type | Description |
|---|---|---|
| id | Int | Unique meal ID |
| name | String | Meal name |
| createdAt | Long | Date created |
| updatedAt | Long | Date last updated |

---

## 14.2 Ingredient

| Field | Type | Description |
|---|---|---|
| id | Int | Unique ingredient ID |
| mealId | Int | Related meal ID |
| name | String | Ingredient name |
| quantity | Double | Numeric amount |
| unit | String | Unit such as g, ml, item, tbsp |

---

## 14.3 MealPlanEntry

| Field | Type | Description |
|---|---|---|
| id | Int | Unique plan entry ID |
| dayOfWeek | Int | 1 = Monday, 7 = Sunday |
| slotNumber | Int | Meal slot number for that day |
| mealId | Int | Assigned meal ID |
| weekStart | Long | Start date of the relevant week |

---

## 14.4 ShoppingItem

| Field | Type | Description |
|---|---|---|
| id | Int | Unique shopping item ID |
| name | String | Ingredient or item name |
| quantity | Double | Total calculated quantity |
| unit | String | Unit |
| isChecked | Boolean | Whether item has been checked off |
| isManual | Boolean | Whether item was manually added |

---

## 14.5 UserPreference

This can be stored in SharedPreferences.

| Field | Type | Description |
|---|---|---|
| mealsPerDay | Int | Default number of meal slots per day |
| shoppingReminderEnabled | Boolean | Whether shopping reminders are enabled |
| shoppingReminderDay | Int | Selected reminder day |
| shoppingReminderTime | String | Selected reminder time |
| mealPrepReminderEnabled | Boolean | Whether prep reminders are enabled |
| mealPrepReminderTime | String | Selected prep reminder time |

---

## 15. Local storage plan

MealMate will use Room database for structured local data.

Room will store:

- meals,
- ingredients,
- weekly plan entries,
- shopping list items.

SharedPreferences will store:

- default meals per day,
- reminder settings,
- simple app preferences.

Firebase will not be used.

---

## 16. ContentProvider plan

MealMate will include a custom ContentProvider named:

```kotlin
MealMateContentProvider
```

### Authority

```text
com.example.mealmate.provider
```

### Example URIs

```text
content://com.example.mealmate.provider/meals
content://com.example.mealmate.provider/meals/{id}
content://com.example.mealmate.provider/ingredients
content://com.example.mealmate.provider/shopping
content://com.example.mealmate.provider/shopping/{id}
```

### Supported operations

| Operation | Purpose |
|---|---|
| query() | Retrieve meals, ingredients, or shopping items |
| insert() | Add a meal, ingredient, or shopping item |
| update() | Update a meal or shopping item |
| delete() | Delete a meal or shopping item |

### Instrumented tests

The app will include instrumented tests to verify:

1. a meal can be inserted through the ContentProvider,
2. a meal can be queried through the ContentProvider,
3. a meal can be updated through the ContentProvider,
4. a meal can be deleted through the ContentProvider,
5. a shopping item can be queried through the ContentProvider,
6. invalid URIs are handled safely.

---

## 17. Permissions and security

### Required permission

For Android 13 and above:

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

### Permission rationale

The app requests notification permission only when the user enables reminders.

### Permissions deliberately not requested

MealMate will not request:

- camera,
- microphone,
- contacts,
- location,
- external storage.

### Security decisions

MealMate will:

- follow least-privilege permission use,
- avoid unnecessary personal data,
- store data locally,
- validate user input,
- avoid exporting components unless required,
- limit ContentProvider exposure,
- handle invalid ContentProvider URIs safely.

---

## 18. Lifecycle handling

MealMate must behave correctly during lifecycle changes such as rotation.

### Expected behaviour

When the screen rotates:

- entered meal names should remain,
- unsaved ingredient entries should remain,
- selected meal count should remain,
- weekly plan should remain visible,
- checked shopping items should remain,
- navigation state should not break.

### Implementation approach

Use:

- ViewModel for UI state,
- SavedStateHandle for form state,
- Room for persisted data,
- lifecycle-aware observers,
- `viewLifecycleOwner` in fragments.

---

## 19. Architecture

Recommended architecture:

```text
UI Layer
- MainActivity
- Fragments
- XML layouts
- ViewModels

Domain / Logic Layer
- ShoppingListGenerator
- IngredientCombiner
- ReminderScheduler
- InputValidator

Data Layer
- Room database
- Entity classes
- DAO interfaces
- Repository classes
- SharedPreferences
- ContentProvider
```

This structure keeps the app easier to build, test, and explain during the demo.

---

## 20. UI/UX design principles

MealMate should be:

- simple,
- clean,
- easy to read,
- fast to use,
- focused on meal planning and shopping.

### Suggested visual style

- light neutral background,
- green accent colour,
- rounded meal cards,
- bottom navigation,
- checkbox shopping list,
- clear weekly plan layout.

---

## 21. Meal categories clarification

Meal categories are optional labels that can help users organise meals. They do not replace the user's own meal creation.

For example, when a user creates a meal, they could optionally label it as:

- high protein,
- vegetarian,
- quick meal,
- breakfast,
- lunch,
- dinner,
- budget meal,
- meal prep friendly.

This would allow filtering later, such as “show me high-protein meals” or “show quick meals”.

However, for the coursework MVP, categories are not necessary. Since users are already creating their own meals, categories should be treated as a **could-have** feature, not a must-have. The app is strong enough without them.

---

## 22. MVP scope

### Must-have

- Meal List Screen
- Create Meal Screen
- Meal Detail Screen
- Weekly Plan Screen
- Shopping List Screen
- Settings / Reminders Screen
- User-selected meals per day
- Combined shopping list totals
- Room local database
- Navigation Component
- ViewModel lifecycle handling
- Android ShareSheet
- Notifications
- Responsive layouts
- Custom ContentProvider
- Instrumented ContentProvider tests

### Should-have

- Edit saved meals
- Delete meals
- Manual shopping list items
- Check off shopping list items
- Reminder settings

### Could-have

- meal categories,
- favourite meals,
- dark mode,
- custom meal slot names,
- weekly plan reset,
- tablet two-pane layout.

### Won't-have

- Firebase,
- login,
- AI meal generation,
- nutrition tracking,
- barcode scanning,
- payments,
- social features.

---

## 23. Acceptance criteria

### Meal creation

Given the user enters a meal name and ingredients, when they press Save, then the meal is stored locally and appears in the Meal List.

### Meal count selection

Given the user selects how many meals they want per day, when they open the Weekly Plan, then each day shows that number of meal slots.

### Weekly planning

Given the user has saved meals, when they assign a meal to a slot, then the slot displays the assigned meal.

### Shopping list generation

Given the user has planned meals, when they generate a shopping list, then all ingredients from planned meals appear in the Shopping List screen.

### Duplicate ingredient combining

Given multiple planned meals contain the same ingredient with the same unit, when the shopping list is generated, then the quantities are combined into one total.

### ShareSheet

Given the user has a shopping list, when they press Share List, then Android ShareSheet opens with the formatted list.

### Notifications

Given the user enables reminders, when the selected reminder time occurs, then the app displays a notification.

### Lifecycle

Given the user is creating a meal, when the screen rotates, then the form state is preserved.

### ContentProvider

Given an instrumented test inserts a meal through the ContentProvider, when the meal is queried, then the inserted meal is returned correctly.

---

## 24. Demo plan

The final demo should be no longer than 15 minutes.

### 24.1 Introduction

Explain:

- what MealMate is,
- who it is for,
- what problem it solves.

### 24.2 Design overview

Show:

- mock screens,
- navigation flow,
- main user journey.

### 24.3 Functionality demo

Demonstrate:

1. selecting meals per day,
2. creating a meal,
3. adding ingredients,
4. saving the meal,
5. assigning meals to the week,
6. generating a combined shopping list,
7. checking off items,
8. sharing the shopping list,
9. enabling reminders,
10. rotating the device to show lifecycle handling.

### 24.4 Implementation overview

Explain:

- Kotlin project structure,
- Navigation Component,
- Room database,
- ViewModel,
- ContentProvider,
- instrumented tests,
- notification permission handling,
- ingredient-combining logic.

### 24.5 Reflection

Discuss:

- mobile app development best practices,
- security decisions,
- responsible permissions,
- lifecycle handling,
- limitations,
- possible future improvements.

---

## 25. Risks and mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Scope becomes too large | Project may not be completed | Keep categories and custom slot names as could-have features |
| Ingredient combining becomes complex | Shopping list totals may be wrong | Only combine when name and unit match |
| ContentProvider takes time | Mandatory requirement at risk | Implement early using meals and shopping tables |
| Notification permission issues | Optional feature may fail | Request permission only when enabling reminders |
| Rotation loses form data | Lifecycle marks at risk | Use ViewModel and SavedStateHandle |
| UI becomes cluttered | Usability marks reduced | Use simple meal cards and clear weekly plan layout |

---

## 26. Final product statement

MealMate is a local-first native Android meal planning app built in Kotlin. It allows users to create their own meals, choose how many meals they want to plan per day, assign meals to a weekly plan, generate a combined shopping list with calculated totals, share the list through Android ShareSheet, and receive shopping or meal-prep reminders. It is designed to meet the 25COB155 coursework specification through native Android development, local storage, Navigation Component, lifecycle-aware implementation, responsible permissions, a custom ContentProvider, instrumented tests, notifications, ShareSheet, and responsive layouts.
