# Price Tracker - Android App

A modern Material 3 Android app that tracks product prices from major Indian e-commerce portals.

## Features

- **Multi-Store Support**: Track prices from Amazon.in, Flipkart, Myntra, and any other online store
- **Easy Product Adding**: Paste a URL directly or share a link from your browser to the app
- **Auto-Fetching**: Automatically scrapes product name, image, description, and current price
- **Price History Chart**: Visual price history graph for each tracked product
- **Real-Time Price Tracking**: Background worker checks prices every 6 hours
- **Smart Notifications**: Alerts when prices drop or change significantly
- **Material 3 UI**: Modern, clean interface with dynamic theming support
- **Edge-to-Edge**: Full edge-to-edge display support

## Screenshots

The app has three main screens:
1. **Home Screen** - Shows all tracked products with current prices, lowest/highest price badges
2. **Add Product Screen** - Paste or receive shared URLs, with supported store chips
3. **Product Detail Screen** - Full product info, price stats (lowest/current/highest), price history chart, description

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM + Repository Pattern
- **Database**: Room (SQLite)
- **Dependency Injection**: Hilt (Dagger)
- **Background Work**: WorkManager
- **Web Scraping**: Jsoup
- **Image Loading**: Coil
- **Navigation**: Jetpack Navigation Compose

## Building

### Prerequisites
- Android SDK (API 34)
- JDK 17+

### Build Debug APK
```bash
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`

### Install on Device
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## How to Use

1. **Add a product**: Tap the + button and paste a product URL from any supported store
2. **Share to app**: From your browser, share a product page link — Price Tracker will appear as a share target
3. **View details**: Tap any product to see detailed price info and history chart
4. **Refresh prices**: Pull to refresh or tap the refresh icon to check for price updates
5. **Notifications**: The app checks prices every 6 hours in the background and notifies you of changes

## Supported Stores

| Store | Status |
|-------|--------|
| Amazon.in | ✅ Supported |
| Flipkart | ✅ Supported |
| Myntra | ✅ Supported |
| Other stores | ✅ Generic scraper (best-effort) |

## Project Structure

```
app/src/main/java/com/pricetracker/app/
├── data/
│   ├── local/          # Room database, DAOs
│   ├── model/          # Data entities (Product, PriceHistory)
│   ├── repository/     # Repository layer
│   └── scraper/        # Web scrapers for each store
├── di/                 # Hilt dependency injection modules
├── notification/       # Notification helper
├── ui/
│   ├── components/     # Reusable UI components (ProductCard, PriceChart)
│   ├── navigation/     # Navigation graph
│   ├── screens/        # Screen composables and ViewModels
│   └── theme/          # Material 3 theme configuration
└── worker/             # Background price check worker
```

## Permissions

- `INTERNET` - For fetching product details and prices
- `ACCESS_NETWORK_STATE` - To check network availability
- `POST_NOTIFICATIONS` - For price change alerts
- `RECEIVE_BOOT_COMPLETED` - To reschedule background checks after reboot
