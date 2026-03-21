# PriceTracker Android App

A modern Android app for tracking product prices from Amazon.in, Flipkart, and Myntra.

## Features

- **Add products** by pasting a URL or sharing from any browser/app
- **Automatic price scraping** from Amazon.in, Flipkart.com, Myntra.com
- **Price history chart** with visual graph showing price trends over time
- **Price drop notifications** - get notified when tracked product prices fall
- **Background price checking** - periodic automatic price updates via WorkManager
- **Material 3 UI** - modern, clean interface following Google's Material You design

## Installation

1. Download `PriceTracker-debug.apk`
2. Enable "Install from Unknown Sources" on your Android device
3. Install the APK
4. Grant notification permissions when prompted

## Usage

### Adding a Product
- Open the app and tap the "Add Product" FAB
- Paste the product URL (Amazon, Flipkart, or Myntra product page)
- Tap "Track Price"

### Sharing from Browser
- While viewing a product page in Chrome/any browser, tap Share
- Select "Price Tracker" from the share sheet
- The URL is automatically populated

### Viewing Price History
- Tap any product card to view detailed price history
- The chart shows price over time with low/current/high indicators

### Notifications
- Price drop alerts are sent automatically when a tracked product's price decreases
- Background checks run every 6 hours (requires internet connection)

## Architecture

- **MVVM** with Jetpack Compose UI
- **Room** database for products and price history
- **Jsoup** for HTML scraping
- **Hilt** dependency injection
- **WorkManager** for background tasks
- **Vico** charts for price history visualization

## Build

```bash
export ANDROID_HOME=/path/to/android/sdk
./PriceTracker/gradlew assembleDebug
```

APK output: `PriceTracker/app/build/outputs/apk/debug/app-debug.apk`
