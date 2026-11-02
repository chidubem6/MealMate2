# MealMate

A native Android calorie and macro tracker, built in Kotlin for a university mobile development module.

## Features

- **Food diary:** log foods to a daily diary and see progress against a calorie goal
- **Food search:** look up foods and serving sizes through the FatSecret API
- **Custom foods and meal templates:** save your own foods and reuse common meals
- **Macro goals:** set a calorie goal and carb, fat and protein percentages
- **Progress:** track weight on a custom-drawn line chart
- **Water tracking**
- **Reminders:** scheduled notifications that are restored after a reboot
- **Content provider:** a private interface to meal, ingredient and shopping data, not exported to other apps

## Tech stack

Kotlin, XML layouts, Fragments, Navigation Component, ViewModel and LiveData, Room, Retrofit and OkHttp. Minimum SDK 24, target SDK 36.

## Getting started

Open the project in Android Studio. To use food search, add [FatSecret API](https://platform.fatsecret.com/) credentials to `local.properties` in the project root:

```properties
FATSECRET_CLIENT_ID=your_client_id
FATSECRET_CLIENT_SECRET=your_client_secret
```

The file is gitignored. The app still builds without these values, but food search won't work.

```bash
./gradlew assembleDebug
```

## Testing

```bash
./gradlew testDebugUnitTest          # macro goals, ingredient combining, servings, meal summaries
./gradlew connectedDebugAndroidTest  # content provider tests, needs a device or emulator
```

## Project layout

Code is under `app/src/main/java/com/example/mealmate2/`.

| Package | Purpose |
|---|---|
| `data/` | Room entities, DAOs, repositories and user preferences |
| `domain/` | Macro calculations, ingredient combining and reminder scheduling |
| `network/` | FatSecret API client and models |
| `provider/` | The content provider |
| `ui/` | Screens, view models and custom chart views |

## Known gaps

The original brief describes a meal-planning app. The weekly plan and shopping list exist in the data layer, but they don't have complete screens yet. See the [compliance audit](docs/COURSEWORK_COMPLIANCE_AUDIT.md) for details.
