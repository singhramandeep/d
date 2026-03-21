# Price Tracker - Android App

A modern Android app built with Material 3 that tracks product prices from Flipkart, Amazon.in, and Myntra.

## Features

- **Add products** – Paste product URLs directly or share links from browsers/apps
- **Automatic scraping** – Fetches product title, image, description, and price
- **Price history** – View price trends over time with an interactive graph
- **Background tracking** – WorkManager checks prices every 4 hours
- **Notifications** – Alerts when tracked product prices drop

## Supported Sites

- flipkart.com
- amazon.in
- myntra.com

## Installation

1. Download `PriceTracker-debug.apk` (or build from source)
2. On your Android device, enable "Install from unknown sources"
3. Install the APK
4. Grant notification permission when prompted (for price drop alerts)

## Building from Source

### Prerequisites

- Android SDK (Android 34+, Build Tools 34+)
- JDK 17 or 21
- Gradle 8.7+

### Build

```bash
# Create local.properties with SDK path:
echo "sdk.dir=/path/to/your/android-sdk" > local.properties

# Build debug APK
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

## Usage

1. **Add a product**: Tap the + button and paste a product URL from Flipkart, Amazon.in, or Myntra
2. **Share to add**: In a browser or app, use "Share" and select "Price Tracker"
3. **View details**: Tap a product to see full details and price history chart
4. **Manual refresh**: Use the refresh button to check for price updates

## Tech Stack

- Kotlin, Jetpack Compose, Material 3
- Room database
- WorkManager for background price checks
- Jsoup for web scraping
- Coil for image loading

## Notes

- E-commerce sites may occasionally block automated requests; if scraping fails, try again later
- Price checks run every 4 hours in the background
- Enable notifications for price drop alerts
