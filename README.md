# PricePulse (Android)

PricePulse is an Android app that lets you share product links from other apps (Amazon, Flipkart, browsers, etc.) and track prices over time.

## Features (MVP)

- Share a product URL from any Android app to PricePulse.
- Supports scraping `amazon.in`, `flipkart.com`, and generic product pages.
- Stores tracked products and price history locally using Room.
- Shows current price and a compact in-app history trail.
- Lets you set a target price threshold per product.
- Runs periodic background checks with WorkManager.
- Sends notifications when the price reaches your target threshold.

## Tech stack

- Kotlin + Jetpack Compose UI
- Room (local database)
- WorkManager (scheduled price refresh)
- OkHttp + Jsoup (HTML fetch/parsing)

## Project structure

- `app/src/main/java/com/example/pricetracker/MainActivity.kt`  
  Entry point, share intent handling, UI wiring.
- `app/src/main/java/com/example/pricetracker/data/`  
  Repository and scraping logic.
- `app/src/main/java/com/example/pricetracker/data/local/`  
  Room entities, DAOs, and database.
- `app/src/main/java/com/example/pricetracker/ui/`  
  Compose screen + ViewModel.
- `app/src/main/java/com/example/pricetracker/worker/`  
  Periodic price-check worker.
- `app/src/main/java/com/example/pricetracker/notifications/`  
  Notification channel + price alert notifications.

## How to run

1. Open the project in Android Studio (latest stable).
2. Let Gradle sync dependencies.
3. Run the `app` module on a device/emulator.
4. Share any product link (text URL) to PricePulse.
5. Add target price and refresh.

## Important notes

- E-commerce sites can change HTML frequently; selectors may need periodic updates.
- Some product pages may block automated requests or require logged-in sessions.
- Respect target website terms of service when scraping content.