# PRD: MealMate — Native Android Calorie Tracker App

## 1. Product overview

**App name:** MealMate  
**Platform:** Native Android  
**Language:** Kotlin  
**Development environment:** Android Studio  
**Coursework:** 25COB155 Mobile Application Development  
**Development approach:** Individual coursework project built from scratch using Kotlin and native Android APIs.

MealMate is a local-first Android calorie tracker that helps users log food across daily meal slots, track calories and macros against a personal daily goal, view progress at a glance, and build an awareness of their nutritional intake over time — similar in concept to MyFitnessPal but intentionally scoped for a coursework project.

The app is designed to satisfy the 25COB155 coursework specification while staying realistic to build within the coursework timeframe.

---

## 2. Confirmed product decisions

1. Users create their own custom foods with calorie and macro information.
2. Users log food entries against named meal slots (Breakfast, Lunch, Dinner, Snacks).
3. Daily calorie and macro totals are calculated automatically from logged entries.
4. The app will be local-only.
5. Firebase will not be used.
6. The selected optional features are:
   - Android ShareSheet,
   - Notifications,
   - adaptation for different screen sizes.
7. Users set a personal daily calorie goal in Settings.
8. Macros tracked are protein, carbohydrates, and fat.

---

## 3. Problem statement

Many people want to track what they eat and stay within a daily calorie target, but popular apps like MyFitnessPal require account creation, network access, and navigate a complex feature set that can feel overwhelming.

MealMate solves this by letting users create foods, log them to daily meal slots, and instantly see their remaining calories and macro breakdown for the day — all stored locally on-device with no login required.

---

## 4. Target users

### Primary target user

People who want to be aware of their calorie and macro intake without the complexity of a full nutrition platform.

### Example users

- Someone counting calories to lose or maintain weight.
- Someone tracking protein intake for fitness goals.
- Someone who wants a quick daily log without creating an account.
- Someone meal-prepping who wants to verify their macros ahead of time.

---

## 5. Product goals

### Functional goals

1. Allow users to create custom foods with name, calories, protein, carbohydrates, and fat per serving.
2. Allow users to log a food to a meal slot (Breakfast, Lunch, Dinner, Snacks) on a given day.
3. Display a daily summary screen showing calories consumed, calories remaining, and macro totals.
4. Allow users to set a personal daily calorie goal.
5. Allow users to view and edit their food log for any given day.
6. Allow users to delete logged food entries.
7. Allow users to share their daily summary through Android ShareSheet.
8. Allow users to enable a daily logging reminder notification.

### Coursework goals

1. Build a native Android app using Kotlin.
2. Use the Navigation Component to move between screens.
3. Use an external Intent through Android ShareSheet.
4. Use local storage (Room and SharedPreferences).
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
- login or user accounts,
- a pre-built food database or barcode scanning,
- meal planning or shopping lists,
- exercise or step tracking,
- water intake tracking,
- weight logging,
- social feeds,
- payment features,
- AI suggestions.

The app is deliberately local-only and focused on calorie logging so the implementation stays within the coursework timeframe.

---

## 7. Coursework requirement mapping

| Coursework requirement | MealMate implementation |
|---|---|
| Native Android app | Built in Android Studio using Kotlin |
| Minimum two screens | App includes Dashboard, Food Log, Add Food Entry, Food Library, Add Custom Food, and Settings screens |
| Navigation Component | Used for all internal screen navigation |
| Intent to outside app | Share daily summary via Android ShareSheet |doe
| Lifecycle handling | ViewModel and SavedStateHandle preserve UI state on rotation |
| Responsible permissions | Notification permission requested only when the user enables reminders |
| Local storage | Room database and SharedPreferences |
| Custom ContentProvider | `MealMateContentProvider` exposes food and log data |
| Instrumented tests | CRUD tests for ContentProvider |
| Optional feature 1 | Android ShareSheet |
| Optional feature 2 | Notifications |
| Optional feature 3 | Adaptation for different screen sizes |
| Code quality | Clear architecture, readable Kotlin, separation of concerns |
| Demo | Clear proposal, design, implementation overview, and security reflection |

---

## 8. Selected optional features

## 8.1 Android ShareSheet

Users can share their daily summary as plain text using Android ShareSheet.

Example shared output:

```text
MealMate — Daily Summary (20 May 2026)

Calories: 1,640 / 2,000 kcal
Protein: 98g
Carbs: 180g
Fat: 52g

Breakfast
- Oats with milk — 320 kcal
- Banana — 90 kcal

Lunch
- Chicken wrap — 510 kcal

Dinner
- Salmon and rice — 620 kcal

Snacks
- Greek yoghurt — 100 kcal
```

This satisfies the coursework requirement to use an Intent to move to an outside app.

---

## 8.2 Notifications

Users can enable a daily logging reminder.

On Android 13 and above, the app will request the `POST_NOTIFICATIONS` permission only when the user enables the reminder.

Example notification:

```text
Title: MealMate Reminder
Body: Don't forget to log your meals today!
```

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

## 9. Meal slots

MealMate uses four fixed daily meal slots:

1. **Breakfast**
2. **Lunch**
3. **Dinner**
4. **Snacks**

This is simpler than a configurable slot system and appropriate for a calorie tracking MVP. Each slot can contain multiple food entries on a given day.

---

## 10. Calorie and macro calculation

### Daily totals

The daily totals are computed by summing all food entries logged on the selected date.

```text
Daily calories consumed = sum of (calories × servings) for all entries on that date
Remaining = goal − consumed
```

Macros are summed the same way:

```text
Total protein = sum of (protein × servings) for all entries
Total carbs = sum of (carbs × servings) for all entries
Total fat = sum of (fat × servings) for all entries
```

### Serving size

When logging a food, the user enters a serving multiplier (e.g., 1, 0.5, 2). The stored food values represent one serving. The multiplier scales calories and macros at log time.

---

## 11. Core features

## Feature 1: Dashboard

### Description

The dashboard is the home screen. It shows the user's calorie progress and macro breakdown for the current day, and provides quick access to logging food.

### Functional requirements

The app shall:

- display today's date,
- display calories consumed and calories remaining against the daily goal,
- display a macro breakdown (protein, carbohydrates, fat),
- list logged food entries grouped by meal slot,
- allow the user to navigate to a previous or next day,
- allow the user to add a food entry from the dashboard,
- allow the user to delete a food entry from the dashboard,
- allow the user to share the daily summary.

### Main UI elements

- date header with previous/next day navigation,
- calorie progress display (consumed / goal),
- macro summary row,
- meal slot sections (Breakfast, Lunch, Dinner, Snacks),
- food entry rows within each slot,
- Add Food button per slot,
- Share button.

---

## Feature 2: Add food entry

### Description

Users can log a food from their food library to a meal slot.

### Functional requirements

The app shall allow users to:

- select a meal slot,
- search or browse the food library,
- select a food,
- enter a serving multiplier,
- confirm and save the entry.

### Main UI elements

- meal slot selector,
- food search or list,
- serving size input,
- calorie preview for selected food and serving,
- Confirm button.

---

## Feature 3: Food library

### Description

Users can view all custom foods they have created.

### Functional requirements

The app shall allow users to:

- view a list of saved foods,
- view calorie and macro details for each food,
- create a new custom food,
- edit a saved food,
- delete a saved food.

### Main UI elements

- food list with calorie and macro summary per item,
- Add Food button,
- edit and delete actions per food.

---

## Feature 4: Add custom food

### Description

Users can create a custom food entry with nutritional information.

### Functional requirements

The app shall allow users to:

- enter a food name,
- enter calories per serving,
- enter protein per serving (grams),
- enter carbohydrates per serving (grams),
- enter fat per serving (grams),
- save the food to the local food library.

### Main UI elements

- food name input,
- calories input,
- protein input,
- carbs input,
- fat input,
- Save button.

---

## Feature 5: Settings

### Description

Users can manage their daily calorie goal and reminder preferences.

### Functional requirements

The app shall allow users to:

- set a daily calorie goal,
- enable or disable a daily logging reminder,
- select reminder time,
- request notification permission only when the reminder is enabled,
- save preferences locally.

### Main UI elements

- calorie goal input,
- reminder toggle,
- time picker,
- permission explanation text,
- Save Settings button.

---

## 12. Screens

MealMate will include the following screens:

1. **Dashboard Screen**
2. **Add Food Entry Screen**
3. **Food Library Screen**
4. **Add Custom Food Screen**
5. **Settings Screen**

This exceeds the minimum two-screen coursework requirement while remaining achievable.

---

## 13. Navigation flow

```text
Dashboard (home)
 ├── Add Food Entry → Food Library (pick a food) → back to Add Food Entry
 ├── Food Library
 │    └── Add Custom Food
 └── Settings
      └── Enable notification reminder
```

The Share button on the Dashboard triggers the Android ShareSheet (external Intent).

Internal movement will use the Navigation Component.

External movement will use Android ShareSheet.

---

## 14. Data model

## 14.1 Food

| Field | Type | Description |
|---|---|---|
| id | Int | Unique food ID |
| name | String | Food name |
| caloriesPerServing | Double | Calories in one serving |
| proteinPerServing | Double | Protein in grams per serving |
| carbsPerServing | Double | Carbohydrates in grams per serving |
| fatPerServing | Double | Fat in grams per serving |
| createdAt | Long | Date created |

---

## 14.2 FoodLogEntry

| Field | Type | Description |
|---|---|---|
| id | Int | Unique log entry ID |
| foodId | Int | Related food ID |
| mealSlot | String | Breakfast, Lunch, Dinner, or Snacks |
| servings | Double | Serving multiplier |
| logDate | String | Date in ISO format (YYYY-MM-DD) |
| loggedAt | Long | Timestamp of log action |

---

## 14.3 UserPreference

Stored in SharedPreferences.

| Field | Type | Description |
|---|---|---|
| dailyCalorieGoal | Int | User's target daily calories |
| reminderEnabled | Boolean | Whether the daily reminder is on |
| reminderTime | String | Selected reminder time (HH:mm) |

---

## 15. Local storage plan

MealMate will use Room database for structured local data.

Room will store:

- foods,
- food log entries.

SharedPreferences will store:

- daily calorie goal,
- reminder settings.

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
content://com.example.mealmate.provider/foods
content://com.example.mealmate.provider/foods/{id}
content://com.example.mealmate.provider/log
content://com.example.mealmate.provider/log/{id}
```

### Supported operations

| Operation | Purpose |
|---|---|
| query() | Retrieve foods or log entries |
| insert() | Add a food or log entry |
| update() | Update a food or log entry |
| delete() | Delete a food or log entry |

### Instrumented tests

The app will include instrumented tests to verify:

1. a food can be inserted through the ContentProvider,
2. a food can be queried through the ContentProvider,
3. a food can be updated through the ContentProvider,
4. a food can be deleted through the ContentProvider,
5. a log entry can be queried through the ContentProvider,
6. invalid URIs are handled safely.

---

## 17. Permissions and security

### Required permission

For Android 13 and above:

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

### Permission rationale

The app requests notification permission only when the user enables the daily reminder.

### Permissions deliberately not requested

MealMate will not request:

- camera,
- microphone,
- contacts,
- location,
- external storage,
- internet.

### Security decisions

MealMate will:

- follow least-privilege permission use,
- avoid collecting personal data beyond what the user enters locally,
- store all data locally on-device,
- validate user input (e.g., calorie values must be numeric and non-negative),
- avoid exporting components unless required,
- limit ContentProvider exposure,
- handle invalid ContentProvider URIs safely.

---

## 18. Lifecycle handling

MealMate must behave correctly during lifecycle changes such as rotation.

### Expected behaviour

When the screen rotates:

- entered food name and nutritional values should remain,
- selected meal slot should remain,
- entered serving size should remain,
- daily totals on the Dashboard should remain visible,
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
- DailyTotalsCalculator
- MacroCalculator
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

- simple and fast to use,
- easy to read with clear calorie numbers,
- focused on the daily log and remaining calories.

### Suggested visual style

- calorie progress shown prominently on the Dashboard,
- macro breakdown as a compact row (protein / carbs / fat),
- meal slot sections clearly separated,
- food cards showing name and calorie value,
- bottom navigation or top navigation bar,
- clean inputs for food creation and logging.

---

## 21. MVP scope

### Must-have

- Dashboard Screen with daily calorie and macro totals
- Add Food Entry Screen
- Food Library Screen
- Add Custom Food Screen
- Settings Screen
- Daily calorie goal setting
- Four fixed meal slots (Breakfast, Lunch, Dinner, Snacks)
- Serving size multiplier
- Room local database
- Navigation Component
- ViewModel lifecycle handling
- Android ShareSheet (share daily summary)
- Notifications (daily logging reminder)
- Responsive layouts
- Custom ContentProvider
- Instrumented ContentProvider tests

### Should-have

- Edit saved foods
- Delete food log entries
- Navigate between days on the Dashboard
- Calories remaining display

### Could-have

- Macro progress bars,
- food search/filter in the food library,
- calorie history chart,
- dark mode,
- tablet two-pane layout.

### Won't-have

- Firebase,
- login,
- pre-built food database,
- barcode scanning,
- exercise tracking,
- social features,
- payments.

---

## 22. Acceptance criteria

### Food creation

Given the user enters a food name and nutritional values, when they press Save, then the food is stored locally and appears in the Food Library.

### Food logging

Given the user selects a food and enters a serving size, when they confirm, then a log entry is created for the selected meal slot and today's date.

### Daily totals

Given the user has logged food entries for today, when they view the Dashboard, then the calories consumed and remaining update to reflect all logged entries.

### Macro breakdown

Given the user has logged food entries, when they view the Dashboard, then protein, carbohydrate, and fat totals are displayed correctly.

### ShareSheet

Given the user has a daily log, when they press Share, then Android ShareSheet opens with a formatted daily summary.

### Notifications

Given the user enables the daily reminder, when the selected reminder time occurs, then the app displays a notification.

### Lifecycle

Given the user is creating a custom food, when the screen rotates, then the entered values are preserved.

### ContentProvider

Given an instrumented test inserts a food through the ContentProvider, when the food is queried, then the inserted food is returned correctly.

---

## 23. Demo plan

The final demo should be no longer than 15 minutes.

### 23.1 Introduction

Explain:

- what MealMate is,
- who it is for,
- what problem it solves.

### 23.2 Design overview

Show:

- mock screens,
- navigation flow,
- main user journey.

### 23.3 Functionality demo

Demonstrate:

1. setting a daily calorie goal,
2. creating a custom food with calories and macros,
3. logging a food to a meal slot,
4. viewing daily calorie and macro totals on the Dashboard,
5. logging multiple foods across different meal slots,
6. sharing the daily summary,
7. enabling the daily reminder notification,
8. rotating the device to show lifecycle handling.

### 23.4 Implementation overview

Explain:

- Kotlin project structure,
- Navigation Component,
- Room database,
- ViewModel,
- ContentProvider,
- instrumented tests,
- notification permission handling,
- calorie and macro calculation logic.

### 23.5 Reflection

Discuss:

- mobile app development best practices,
- security decisions,
- responsible permissions,
- lifecycle handling,
- limitations,
- possible future improvements.

---

## 24. Risks and mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Scope becomes too large | Project may not be completed | Keep macro charts and history as could-have features |
| Serving size calculation errors | Incorrect daily totals | Validate serving input and test calculation logic |
| ContentProvider takes time | Mandatory requirement at risk | Implement early using foods and log tables |
| Notification permission issues | Optional feature may fail | Request permission only when enabling the reminder |
| Rotation loses form data | Lifecycle marks at risk | Use ViewModel and SavedStateHandle |
| UI becomes cluttered | Usability marks reduced | Show calories prominently, keep macro row compact |

---

## 25. Final product statement

MealMate is a local-first native Android calorie tracker built in Kotlin. It allows users to create custom foods with nutritional information, log those foods to daily meal slots, and instantly see their calorie and macro totals against a personal daily goal — similar in concept to MyFitnessPal but scoped for local, account-free use. It is designed to meet the 25COB155 coursework specification through native Android development, local storage, Navigation Component, lifecycle-aware implementation, responsible permissions, a custom ContentProvider, instrumented tests, notifications, ShareSheet, and responsive layouts.
