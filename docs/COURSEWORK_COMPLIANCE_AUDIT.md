# MealMate Coursework Compliance Audit

## Verified in code

- Native Android app in Kotlin with XML layouts and Fragment navigation.
- Navigation Component is configured in `app/src/main/res/navigation/nav_graph.xml`.
- Room local storage is configured in `MealMateDatabase`.
- SharedPreferences are used through `UserPreferences`.
- Android 13 notification permission is requested only when reminders are enabled.
- Reminder scheduling exists through `ReminderScheduler`, `ReminderReceiver`, and `BootReceiver`.
- Responsive resources exist under `layout-w600dp` and `values-w600dp`.
- Custom `MealMateContentProvider` is registered in the manifest with authority `com.example.mealmate2.provider`.
- ContentProvider instrumented tests compile for meals and shopping items.
- Ingredient combining is implemented and unit-tested in `IngredientCombiner`.

## Fixed in this pass

- Added coursework entities for `meals`, `ingredients`, and `shopping_items`.
- Added the missing production `MealMateContentProvider`.
- Registered the provider in `AndroidManifest.xml`.
- Added `mealsPerDay`, shopping reminder, and meal-prep reminder preference fields.
- Added tested duplicate ingredient combining for matching trimmed case-insensitive names and matching units.

## Remaining coursework risks

- The current UI is still mainly a calorie and macro tracker, not the PRD's meal-planning workflow.
- There is no complete Weekly Plan screen for Monday to Sunday meal slots.
- There is no complete Shopping List screen connected to weekly plan generation.
- Android ShareSheet is not implemented in the app code yet.
- The manifest still requests `INTERNET`, and FatSecret API code is still present, which conflicts with the PRD's local-only product decision.
- Create/edit meal and meal-detail screens in the PRD are not fully represented as user-created meals with ingredients.
- The instrumented ContentProvider tests compile, but they still need to be run on an emulator or device with `connectedDebugAndroidTest`.

## Recommended next implementation order

1. Replace or supplement the current nutrition-tracker home flow with Meal List, Create Meal, Meal Detail, Weekly Plan, and Shopping List screens.
2. Connect weekly plan generation to `IngredientCombiner`.
3. Add Share List using `Intent.ACTION_SEND` and `Intent.createChooser`.
4. Remove FatSecret/network usage and the `INTERNET` permission if the final submission must be strictly local-only.
5. Run `connectedDebugAndroidTest` on an emulator/device and record the ContentProvider test result for the demo.
