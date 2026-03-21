# Price Tracker Android App (Material 3)

An Android app built with Kotlin + Jetpack Compose (Material 3) to track product prices from:
- `amazon.in`
- `flipkart.com`
- `myntra.com`

## Implemented features

- Add product URL directly in-app.
- Add product via Android Share sheet (`SEND` text intent).
- Parse product title, image, and current price from product pages.
- Store products and price snapshots in Room database.
- Poll prices every **configurable interval** (1m / 5m / 10m / 15m) while app is active.
- Background fallback polling via WorkManager.
- Price-drop notifications.
- Product detail screen with price history chart and timeline.
- History range filter: **7D / 30D / 90D / ALL**.

## Tech stack

- Kotlin
- Jetpack Compose + Material 3
- Room
- WorkManager
- DataStore Preferences
- Coil

## Build instructions

```bash
./gradle-8.7/bin/gradle testDebugUnitTest
./gradle-8.7/bin/gradle assembleDebug
```

Generated APK:

`app/build/outputs/apk/debug/app-debug.apk`

## Notes

- Site HTML can change over time; parsing is best-effort.
- Background execution frequency is constrained by Android power management rules.