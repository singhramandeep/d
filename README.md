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

### Download the APK (mobile / browser)

1. On GitHub, open this repository and go to the **`releases`** folder in the file tree, or use this direct path: **`releases/PriceTracker-debug.apk`**
2. Open the file, tap **View raw** or **Download** (browser: long-press the **Raw** link and save the file, or use GitHub’s download button on the file page)
3. On your Android device, open the downloaded file from **Downloads** and install
4. If needed, allow **Install unknown apps** for your browser or Files app
5. Grant **notification** permission when prompted (for price drop alerts)

The debug APK in the repo is a convenience build; for production, sign a release build locally.

**Update:** The tracked APK is rebuilt when we fix install/runtime issues. Re-download if you had crashes after adding a product.

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
