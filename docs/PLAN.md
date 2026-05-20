# MealMate Section-by-Section Build Plan

## Summary
Build MealMate as a native Kotlin Android app using XML views, Fragments, Navigation Component, Room, ViewModels, SharedPreferences, notifications, Android ShareSheet, responsive layouts, and a custom `ContentProvider`.

The repo is currently a starter Android project, so implementation should begin with app foundation, then build each feature screen in dependency order so coursework requirements are met early.

## Key Changes

### 1. Foundation And Architecture
- Add AndroidX dependencies for Navigation Component, Room, Lifecycle ViewModel/LiveData or Flow, RecyclerView, ConstraintLayout, WorkManager or AlarmManager support, and Room testing.
- Create package structure:
  - `data`: Room database, entities, DAOs, repositories.
  - `domain`: shopping list generation, ingredient combining, validation, reminders.
  - `ui`: fragments, adapters, viewmodels.
  - `provider`: custom `MealMateContentProvider`.
- Replace the empty app manifest setup with `MainActivity`, notification permission, provider declaration, and navigation host.
- Use XML layouts and Fragment-based navigation because this matches the PRD and coursework mapping.

### 2. Data Layer First
- Implement Room entities:
  - `MealEntity`
  - `IngredientEntity`
  - `MealPlanEntryEntity`
  - `ShoppingItemEntity`
- Implement DAOs for meals, ingredients, weekly plan entries, and shopping items.
- Store user preferences in SharedPreferences:
  - meals per day
  - shopping reminder settings
  - meal prep reminder settings
- Add repositories so UI code does not access Room directly.
- Performance defaults:
  - use indexed columns for `mealId`, `dayOfWeek`, `weekStart`, and shopping item lookup.
  - use suspend DAO methods or Flow queries.
  - avoid querying ingredients one meal at a time when generating shopping lists.

### 3. Meal List Section
- Build the Meal List screen as the app start destination.
- Show saved meals in a RecyclerView.
- Add actions for:
  - open meal detail
  - create meal
  - edit meal
  - delete meal
- Use `ListAdapter` with `DiffUtil` for efficient RecyclerView updates.
- Show an empty state when no meals exist.

### 4. Create/Edit Meal Section
- Build one reusable Create/Edit Meal screen.
- Support:
  - meal name input
  - ingredient name input
  - numeric quantity input
  - unit input
  - ingredient preview list
  - add/remove ingredient before saving
  - save meal locally
- Preserve unsaved form state with ViewModel and `SavedStateHandle`.
- Validate:
  - meal name is not blank
  - ingredient name is not blank
  - quantity is numeric and greater than zero
  - unit is not blank
- Save meal and ingredients in one Room transaction.

### 5. Meal Detail Section
- Display meal name and ingredients.
- Provide Edit and Delete actions.
- Delete should remove related ingredients and plan references safely.
- Use a single query/repository call that returns the meal with ingredients to avoid extra UI-layer database work.

### 6. Weekly Plan Section
- Display Monday to Sunday.
- For each day, render slots based on the selected meals-per-day preference.
- Slot labels should be `Meal 1`, `Meal 2`, etc. for MVP.
- Allow assigning, changing, and removing a saved meal from each slot.
- Store assignments in `MealPlanEntryEntity` with `dayOfWeek`, `slotNumber`, `mealId`, and `weekStart`.
- Add a Generate Shopping List button that builds shopping items from the current weekly plan.
- Performance defaults:
  - load all plan entries for the week in one query.
  - load all meals needed for the plan in batched queries.
  - avoid recalculating the shopping list on every small UI change; generate only when requested.

### 7. Shopping List Section
- Generate shopping items from planned meals.
- Combine ingredients only when:
  - trimmed lowercase ingredient names match
  - units match
  - quantities are numeric
- Display:
  - item name
  - total quantity
  - unit
  - checked state
- Allow:
  - manual item addition
  - check/uncheck items
  - clear checked items
  - share list through Android ShareSheet
- Share output as plain text headed with `MealMate Shopping List`.
- Preserve manual shopping items when regenerating generated items unless the user clears them.

### 8. Settings And Reminders Section
- Allow selecting default meals per day.
- Allow enabling/disabling shopping reminders and meal-prep reminders.
- On Android 13+, request `POST_NOTIFICATIONS` only when the user enables reminders.
- Schedule reminders locally using an Android-appropriate local scheduling approach.
- Store settings in SharedPreferences.
- Keep reminder logic in a `ReminderScheduler` class so it is easy to explain in the demo.

### 9. ContentProvider Section
- Implement `MealMateContentProvider` with authority:
  - `com.example.mealmate2.provider`
- Supported URIs:
  - `content://com.example.mealmate2.provider/meals`
  - `content://com.example.mealmate2.provider/meals/{id}`
  - `content://com.example.mealmate2.provider/ingredients`
  - `content://com.example.mealmate2.provider/shopping`
  - `content://com.example.mealmate2.provider/shopping/{id}`
- Support `query`, `insert`, `update`, and `delete` for coursework-required data.
- Keep provider non-exported unless coursework explicitly requires external access.
- Handle invalid URIs with safe errors, not crashes.

### 10. Responsive Layout Section
- Use ConstraintLayout and RecyclerView-based layouts.
- Avoid fixed widths and heights.
- Add landscape or `w600dp` layout variants only where the phone layout becomes cramped:
  - Weekly Plan
  - Shopping List
- Keep touch targets at least 48dp.
- Use strings/resources instead of hardcoded UI text.

### 11. Performance Optimization
- Use Room transactions for multi-table writes.
- Use indexes for frequently queried foreign keys and week-plan fields.
- Use RecyclerView + `ListAdapter`/`DiffUtil` on lists.
- Keep shopping list generation off the main thread.
- Avoid repeated database queries inside loops.
- Use ViewModels to avoid reloading data on rotation.
- Keep layouts shallow and avoid nested scrolling where possible.
- Use lazy loading only where useful; this app’s data should remain local and lightweight.
- Run release build checks and fix obvious lint/performance warnings.

## Test Plan
- Unit test `IngredientCombiner`:
  - combines same name/unit with numeric quantities
  - does not combine different units
  - trims and normalises case
- Unit test shopping list generation from multiple planned meals.
- Instrumented Room or repository tests for meal save/delete and weekly plan assignment.
- Instrumented ContentProvider tests:
  - insert meal
  - query meal
  - update meal
  - delete meal
  - query shopping items
  - invalid URI handling
- Manual acceptance tests:
  - create meal
  - rotate during meal creation
  - assign meals to week
  - generate combined shopping list
  - check off items
  - share via Android ShareSheet
  - enable reminders and verify permission flow
  - test portrait and landscape layouts.

## Assumptions
- Use XML layouts, Fragments, and Navigation Component rather than Jetpack Compose.
- Use package authority `com.example.mealmate2.provider` to match the current app namespace.
- Categories, favourites, dark mode, custom slot names, and tablet two-pane layout remain out of MVP.
- Room is the main local database; SharedPreferences is only for simple settings.
- Shopping list generation is user-triggered, not continuously regenerated after every plan edit.
