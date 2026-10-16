<div align="center">

<img src="docs/assets/logo.png" alt="Plant.LK logo" width="140">

# Plant.LK

**A native Android m-commerce application for plants and gardening supplies in Sri Lanka.**

*We grow the plants. You grow the life.*

<p align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white"/>
  <img src="https://img.shields.io/badge/Java%2011-ED8B00?logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/Firebase-FFCA28?logo=firebase&logoColor=black"/>
  <img src="https://img.shields.io/badge/Cloud%20Firestore-FFA000?logo=firebase&logoColor=white"/>
  <img src="https://img.shields.io/badge/Firebase%20Auth-DD2C00?logo=firebase&logoColor=white"/>
  <img src="https://img.shields.io/badge/Firebase%20Cloud%20Messaging-039BE5?logo=firebase&logoColor=white"/>
  <img src="https://img.shields.io/badge/Google%20Maps%20Platform-4285F4?logo=googlemaps&logoColor=white"/>
  <img src="https://img.shields.io/badge/Material%20Design%203-757575?logo=materialdesign&logoColor=white"/>
  <img src="https://img.shields.io/badge/SQLite-003B57?logo=sqlite&logoColor=white"/>
  <img src="https://img.shields.io/badge/PayHere-Payments-00A651"/>
  <img src="https://img.shields.io/badge/Glide-5.0.5-3DDC84"/>
  <img src="https://img.shields.io/badge/ExoPlayer-2.19.1-FF0000?logo=googleplay&logoColor=white"/>
  <img src="https://img.shields.io/badge/Lombok-1.18.42-BC4521"/>
  <img src="https://img.shields.io/badge/Gradle-AGP%209.0.1-02303A?logo=gradle&logoColor=white"/>
  <img src="https://img.shields.io/badge/Android%20Studio-3DDC84?logo=androidstudio&logoColor=white"/>
</p>

</div>

---

## Table of Contents

1. [Overview](#overview)
2. [Features](#features)
3. [Screenshots](#screenshots)
4. [Tech Stack](#tech-stack)
5. [Architecture](#architecture)
6. [Project Structure](#project-structure)
7. [Data Model](#data-model)
8. [Getting Started](#getting-started)
9. [Configuration and Security](#configuration-and-security)
10. [Admin Dashboard](#admin-dashboard)
11. [Testing and Evaluation](#testing-and-evaluation)
12. [Localization](#localization)
13. [Roadmap](#roadmap)
14. [Author](#author)

---

## Overview

Plant buying in Sri Lanka still relies largely on physical markets and nurseries. Customers have no single place to discover plant varieties, compare prices, find nearby nurseries, and pay online. Plant.LK addresses this with a dedicated mobile marketplace that combines a browsable product catalogue, nursery discovery with in-app navigation, secure online payment, and order tracking, backed by a Firebase cloud back end and a web-based admin dashboard.

| | |
|---|---|
| **Application ID** | `lk.susa.plant_lk` |
| **Platform** | Android (minSdk 24, compileSdk / targetSdk 36) |
| **Language** | Java 11 with Lombok |
| **Back end** | Firebase Authentication, Cloud Firestore, Cloud Storage, Cloud Messaging |
| **Payments** | PayHere Android SDK (sandbox mode, currency LKR) |

---

## Features

### Customer application

**Accounts and profile**
- Email and password registration and sign-in through Firebase Authentication, with password reset by email.
- Profile screen with profile photo upload and password change. The registered email is read-only.
- Local notification on successful sign-in; account deletion available from Settings.

**Catalogue and discovery**
- Home screen with company introduction, headline statistics, intro video (ExoPlayer with mute toggle), categories, top-selling products, new arrivals, seasonal offers, featured nurseries, and plant care tips.
- Category grid with search, and per-category product listings.
- Product detail screen with image slider, description, rating, dynamic attributes (for example size), quantity selector, and live stock availability.

**Shopping and checkout**
- Wishlist and cart synchronised per user in Firestore.
- Checkout with pre-filled shipping details, optional separate billing address, validation, and a calculated order total including shipping.
- Online payment through PayHere (cards and local wallets); successful payments create an order document in Firestore.
- Order history with order status and itemised detail view.
- A text receipt is generated for each order, stored in app storage, and exported to the device Documents folder on Android 10 and above.

**Nurseries and maps**
- Google Maps view of partner nurseries with current-location marker and a searchable nursery list.
- Route drawing from the user's location to a selected nursery using the Google Directions API and decoded polylines; nursery details (address, hours, phone, distance) with Call and Get Route actions.
- Shake-to-locate: shaking the device re-centres the map on the user, using the accelerometer with a cooldown.

**Content, support, and settings**
- Plant care guide (watering, sunlight, fertiliser, pruning, pest control, soil care, and more) stored in a local SQLite database, so it remains available offline.
- Contact screen that submits messages to the administrators through Firestore.
- Settings for dark mode, push notifications, and language (seven languages).

**System integration**
- Firebase Cloud Messaging push notifications, including image notifications; the device FCM token is stored on the user document.
- Broadcast receiver reacting to airplane mode, low battery, charger connection, and order placement with local notifications.
- Network state monitoring through `ConnectivityManager` callbacks.

### Administration (web dashboard)
- Product, category, and nursery management (create, update, delete) with image upload to Firebase Storage.
- Order management with status updates (Pending, Confirmed, Processing, Shipped, Delivered, Cancelled).
- Customer message inbox with reply by email.
- User management with role and account-status control.

---

## Screenshots

### Onboarding and navigation
<table>
  <tr><td align="center" valign="top"><img src="docs/screenshots/01-splash.jpg" width="200" alt="Splash"><br><sub>Splash</sub></td><td align="center" valign="top"><img src="docs/screenshots/02-sign-in.jpg" width="200" alt="Sign in"><br><sub>Sign in</sub></td><td align="center" valign="top"><img src="docs/screenshots/03-sign-up.jpg" width="200" alt="Sign up"><br><sub>Sign up</sub></td><td align="center" valign="top"><img src="docs/screenshots/08-side-drawer.jpg" width="200" alt="Navigation drawer"><br><sub>Navigation drawer</sub></td></tr>
</table>

### Browsing the catalogue
<table>
  <tr><td align="center" valign="top"><img src="docs/screenshots/04-home-top.jpg" width="200" alt="Home"><br><sub>Home</sub></td><td align="center" valign="top"><img src="docs/screenshots/05-home-video.jpg" width="200" alt="Intro video"><br><sub>Intro video</sub></td><td align="center" valign="top"><img src="docs/screenshots/06-home-products.jpg" width="200" alt="Top selling and new arrivals"><br><sub>Top selling and new arrivals</sub></td><td align="center" valign="top"><img src="docs/screenshots/07-home-offers.jpg" width="200" alt="Offers and nurseries"><br><sub>Offers and nurseries</sub></td></tr>
  <tr><td align="center" valign="top"><img src="docs/screenshots/09-categories.jpg" width="200" alt="Categories"><br><sub>Categories</sub></td><td align="center" valign="top"><img src="docs/screenshots/10-product-listing.jpg" width="200" alt="Product listing"><br><sub>Product listing</sub></td><td align="center" valign="top"><img src="docs/screenshots/11-product-details.jpg" width="200" alt="Product details"><br><sub>Product details</sub></td><td align="center" valign="top"><img src="docs/screenshots/12-product-details-end.jpg" width="200" alt="Quantity and availability"><br><sub>Quantity and availability</sub></td></tr>
</table>

### Wishlist, checkout, and orders
<table>
  <tr><td align="center" valign="top"><img src="docs/screenshots/13-wishlist.jpg" width="230" alt="Wishlist"><br><sub>Wishlist</sub></td><td align="center" valign="top"><img src="docs/screenshots/14-cart.jpg" width="230" alt="Cart"><br><sub>Cart</sub></td><td align="center" valign="top"><img src="docs/screenshots/15-checkout-shipping.jpg" width="230" alt="Shipping details"><br><sub>Shipping details</sub></td></tr>
  <tr><td align="center" valign="top"><img src="docs/screenshots/16-checkout-billing.jpg" width="230" alt="Billing details"><br><sub>Billing details</sub></td><td align="center" valign="top"><img src="docs/screenshots/17-checkout-summary.jpg" width="230" alt="Order summary"><br><sub>Order summary</sub></td><td align="center" valign="top"><img src="docs/screenshots/18-payment-methods.jpg" width="230" alt="PayHere checkout (sandbox)"><br><sub>PayHere checkout (sandbox)</sub></td></tr>
  <tr><td align="center" valign="top"><img src="docs/screenshots/19-payment-success.jpg" width="230" alt="Payment approved"><br><sub>Payment approved</sub></td><td align="center" valign="top"><img src="docs/screenshots/20-orders.jpg" width="230" alt="My orders"><br><sub>My orders</sub></td><td align="center" valign="top"><img src="docs/screenshots/21-order-detail.jpg" width="230" alt="Order details"><br><sub>Order details</sub></td></tr>
</table>

### Nurseries, care tips, and support
<table>
  <tr><td align="center" valign="top"><img src="docs/screenshots/25-nursery-map.jpg" width="200" alt="Nursery map"><br><sub>Nursery map</sub></td><td align="center" valign="top"><img src="docs/screenshots/26-nursery-list.jpg" width="200" alt="Nursery search"><br><sub>Nursery search</sub></td><td align="center" valign="top"><img src="docs/screenshots/27-nursery-route.jpg" width="200" alt="Route to nursery"><br><sub>Route to nursery</sub></td><td align="center" valign="top"><img src="docs/screenshots/28-nursery-details.jpg" width="200" alt="Nursery details"><br><sub>Nursery details</sub></td></tr>
  <tr><td align="center" valign="top"><img src="docs/screenshots/22-plant-care.jpg" width="200" alt="Plant care tips"><br><sub>Plant care tips</sub></td><td align="center" valign="top"><img src="docs/screenshots/23-plant-care-detail.jpg" width="200" alt="Care detail"><br><sub>Care detail</sub></td><td align="center" valign="top"><img src="docs/screenshots/24-contact.jpg" width="200" alt="Contact us"><br><sub>Contact us</sub></td><td align="center" valign="top"><img src="docs/screenshots/29-profile.jpg" width="200" alt="Profile"><br><sub>Profile</sub></td></tr>
</table>

### Settings
<table>
  <tr><td align="center" valign="top"><img src="docs/screenshots/30-settings.jpg" width="200" alt="Settings"><br><sub>Settings</sub></td><td align="center" valign="top"><img src="docs/screenshots/31-language.jpg" width="200" alt="Language selection"><br><sub>Language selection</sub></td></tr>
</table>

---

## Tech Stack

| Area | Technology |
|---|---|
| Language and build | Java 11, Gradle (Groovy DSL) with version catalog, Android Gradle Plugin 9.0.1 |
| UI | Material Components 1.13, ConstraintLayout, ViewBinding, Navigation Drawer and Bottom Navigation, CircleImageView, DotsIndicator |
| Media | Glide 5.0.5 (image loading and caching), ExoPlayer 2.19.1 (intro video) |
| Cloud | Firebase BoM 34.10.0: Authentication, Cloud Firestore, Cloud Storage, Cloud Messaging, Analytics |
| Maps and location | Google Maps SDK, Play Services Location, Android Maps Utils (polyline decoding), Google Maps Services Java client (Directions API) |
| Payments | PayHere Android SDK v3.0.18 |
| Local storage | SQLite (`SQLiteOpenHelper`), SharedPreferences, internal and MediaStore file storage |
| Utilities | Lombok, Gson, Secrets Gradle Plugin (build-time injection of API keys and credentials) |
| Concurrency | `ExecutorService` and `Handler` for background work (route requests, SQLite reads) |

---

## Architecture

The application is organised in clear layers: a fragment-based presentation layer, plain model classes, and data access that talks directly to the Firebase SDK and to SQLite. Cross-cutting concerns such as notifications, locale handling, and network monitoring live in a utilities package.

```mermaid
flowchart LR
    subgraph UI["Presentation layer"]
        A["Activities<br/>Splash, SignIn, SignUp, Main"]
        F["Fragments<br/>Home, Category, Listing,<br/>Cart, Checkout, Orders,<br/>Nursery Map, Care, ..."]
        AD["Adapters + ViewBinding"]
    end

    subgraph DOMAIN["Model layer"]
        M["Lombok models<br/>User, Product, Order,<br/>CartItem, Nursery, Category"]
    end

    subgraph DATA["Data and services"]
        FS[("Cloud Firestore")]
        ST[("Cloud Storage")]
        AU["Firebase Auth"]
        FCM["Firebase Cloud Messaging"]
        SQ[("SQLite<br/>plant care tips")]
        SP["SharedPreferences<br/>theme, language, notifications"]
        PH["PayHere SDK"]
        GM["Google Maps +<br/>Directions API"]
    end

    U["Utilities<br/>NotificationHelper, NetworkMonitor,<br/>SystemStateReceiver, LocaleHelper,<br/>ReceiptStorage"]

    A --> F --> AD --> M
    F --> FS
    F --> ST
    A --> AU
    FCM --> U
    F --> SQ
    F --> SP
    F --> PH
    F --> GM
    F --> U

    WEB["Web admin dashboard"] --> FS
    WEB --> ST
    WEB --> AU
```

**Key design decisions**
- **Firestore as the source of truth.** Catalogue, nurseries, orders, carts, and wishlists are read from and written to Firestore directly; the admin dashboard writes to the same collections, so changes reach the app without a custom API server.
- **Per-user subcollections.** Cart and wishlist items are stored under `users/{uid}/cart` and `users/{uid}/wishlist`, keeping user data isolated.
- **Off-main-thread work.** Directions requests and SQLite access run on executors and post results back to the UI thread.
- **Local persistence where it matters.** The plant care guide is seeded into SQLite on first launch so it works without connectivity.
- **Credentials injected at build time.** API keys and payment credentials are read from `local.properties` through the Secrets Gradle Plugin and exposed as `BuildConfig` fields, so no secret lives in source control.

---

## Project Structure

```
Plant_LK/
├── app/
│   ├── build.gradle
│   ├── google-services.json.example   Template for your Firebase config
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/lk/susa/plant_lk/
│       │   ├── activity/      Splash, SignIn, SignUp, Main, Base activities
│       │   ├── fragment/      Home, Category, Listing, ProductDetails, Cart, Checkout,
│       │   │                  Orders, OrderDetail, Wishlist, Profile, NurseryMap,
│       │   │                  Care, PlantCareDetail, Contact, Settings
│       │   ├── adapter/       RecyclerView and pager adapters
│       │   ├── model/         User, Product, Category, Nursery, CartItem, Order, PlantCareTip
│       │   ├── database/      DatabaseHelper (SQLite)
│       │   ├── listener/      FirestoreCallback
│       │   └── util/          NotificationHelper, PushNotificationService, NetworkMonitor,
│       │                      SystemStateReceiver, LocaleHelper, ReceiptStorage, TelephonyHelper
│       └── res/               layouts, drawables, menus, themes (light and night),
│                              localized strings, raw intro video
├── docs/                      README assets (logo and screenshots)
├── gradle/libs.versions.toml  Dependency version catalog
├── local.properties.example   Template for local secrets (copy to local.properties)
├── secrets.defaults.properties  Non-secret placeholders so the project always syncs
├── build.gradle
└── settings.gradle
```

---

## Data Model

### Cloud Firestore

| Collection | Key fields | Purpose |
|---|---|---|
| `users` | `uid`, `name`, `email`, `role`, `fcmToken`, `profilePicUrl`, `status`, `updatedAt` | Profiles, roles, and push tokens |
| `users/{uid}/cart` | `productId`, `quantity`, `attributes[]` | Per-user shopping cart |
| `users/{uid}/wishlist` | product references | Per-user saved items |
| `categories` | `categoryId`, `name`, `slug`, `imageUrl`, `sortOrder` | Product categories |
| `products` | `productId`, `title`, `description`, `price`, `categoryId`, `images[]`, `stockCount`, `status`, `rating`, `attributes[]` | Catalogue with dynamic attributes |
| `nurseries` | `name`, `address`, `hours`, `phone`, `latitude`, `longitude` | Nursery locations |
| `orders` | `orderId`, `userId`, `totalAmount`, `status`, `orderDate`, `orderItems[]`, `shippingAddress`, `billingAddress` | Completed orders |
| `messages` | `userId`, `name`, `email`, `subject`, `message`, `timestamp` | Contact requests |

### SQLite (`plant_lk.db`)

| Table | Columns | Purpose |
|---|---|---|
| `plant_tips` | `id`, `title` (unique), `treatments`, `pests`, `side_effects` | Offline plant care guide, seeded on first launch |

---

## Getting Started

### Prerequisites

- Android Studio (a recent release that supports Android Gradle Plugin 9.0.1) and JDK 17 or newer
- An Android device or emulator running Android 7.0 (API 24) or higher, with Google Play services for the map features
- A Firebase project, and a Google Cloud project with billing enabled for the Maps APIs
- A PayHere sandbox merchant account (free) for testing payments

### 1. Clone the repository

```bash
git clone https://github.com/sushensanthush/Plant_LK.git
cd Plant_LK
```

### 2. Configure Firebase

1. Create a Firebase project and register an Android app with the package name `lk.susa.plant_lk`.
2. Add your debug (and release) SHA-1 fingerprints in the Firebase app settings.
3. Download `google-services.json` and place it in `app/` (see `app/google-services.json.example` for the expected structure). The file is git-ignored.
4. Enable the following services:
   - **Authentication** with the Email/Password provider
   - **Cloud Firestore**
   - **Cloud Storage**
   - **Cloud Messaging**
5. Create the collections described in [Data Model](#data-model) and add sample categories, products, and nurseries, or populate them through the admin dashboard.

### 3. Configure local credentials

Copy the template and fill in your own values:

```bash
cp local.properties.example local.properties
```

```properties
MAPS_API_KEY=your_maps_api_key
DIRECTIONS_API_KEY=your_directions_api_key
PAYHERE_MERCHANT_ID=your_payhere_merchant_id
PAYHERE_MERCHANT_SECRET=your_payhere_merchant_secret
```

| Property | Used for |
|---|---|
| `MAPS_API_KEY` | Google Maps SDK for Android (injected into the manifest) |
| `DIRECTIONS_API_KEY` | Directions API requests for nursery routes (may be the same key as above) |
| `PAYHERE_MERCHANT_ID`, `PAYHERE_MERCHANT_SECRET` | PayHere sandbox checkout |

In Google Cloud Console, enable **Maps SDK for Android** and **Directions API**, and restrict your keys by package name, SHA-1 fingerprint, and API. Payments run in PayHere sandbox mode, so no real transactions occur; use the test cards from the PayHere sandbox documentation.

If a property is missing, the build falls back to placeholder values from `secrets.defaults.properties`, so the project still compiles but maps and payments will not work until real values are supplied.

### 4. Build and run

```bash
./gradlew assembleDebug
```

Or open the project in Android Studio, let Gradle sync, and run the `app` configuration. The PayHere SDK is resolved from JitPack, which is declared in `settings.gradle`.

---

## Configuration and Security

- **No secrets in the repository.** `google-services.json` and `local.properties` are git-ignored. Only the `*.example` templates and the placeholder `secrets.defaults.properties` are committed.
- **Rotate any credential that has been exposed.** If a key or merchant secret was ever committed, shared, or pasted anywhere, revoke it and generate a new one.
- **Restrict API keys** by package name, SHA-1 fingerprint, and API in Google Cloud Console.
- **Verify payments server-side for production.** A merchant secret should not ship inside an APK. Production deployments should confirm payments through PayHere's server notification (`notify_url`) on a trusted back end.
- **Apply Firestore and Storage security rules.** Restrict writes on `products`, `categories`, and `nurseries` to administrators, and restrict `users/{uid}` data and `orders` to their owners.
- **Enable release hardening.** `minifyEnabled` is currently `false`; enable R8 and review the ProGuard rules before a release build.

---

## Admin Dashboard

The administrator interface is a separate responsive web application (HTML, CSS, JavaScript, and Bootstrap) that uses Firebase Authentication and the same Firestore and Storage back end as the mobile app. It is **not part of this repository**. Field names in its forms match the Firestore schema used by the Android client, and changes made there appear in the app through live Firestore reads.

<table>
  <tr><td align="center" valign="top"><img src="docs/screenshots/admin-01-login.jpg" width="430" alt="Admin login"><br><sub>Admin login</sub></td><td align="center" valign="top"><img src="docs/screenshots/admin-02-dashboard.jpg" width="430" alt="Dashboard"><br><sub>Dashboard</sub></td></tr>
  <tr><td align="center" valign="top"><img src="docs/screenshots/admin-03-products.jpg" width="430" alt="Product management"><br><sub>Product management</sub></td><td align="center" valign="top"><img src="docs/screenshots/admin-04-add-product.jpg" width="430" alt="Add product"><br><sub>Add product</sub></td></tr>
  <tr><td align="center" valign="top"><img src="docs/screenshots/admin-05-categories.jpg" width="430" alt="Category management"><br><sub>Category management</sub></td><td align="center" valign="top"><img src="docs/screenshots/admin-06-nurseries.jpg" width="430" alt="Nursery management"><br><sub>Nursery management</sub></td></tr>
  <tr><td align="center" valign="top"><img src="docs/screenshots/admin-07-orders.jpg" width="430" alt="Order management"><br><sub>Order management</sub></td><td align="center" valign="top"><img src="docs/screenshots/admin-08-messages.jpg" width="430" alt="Customer messages"><br><sub>Customer messages</sub></td></tr>
</table>

---

## Testing and Evaluation

### Functional testing

All requirements were verified manually against predefined test cases.

| Test case | Expected outcome | Result |
|---|---|---|
| Register with valid credentials | Account created and user redirected to home | Pass |
| Sign in with incorrect password | Error shown, access denied | Pass |
| Go offline and return online | Connectivity change detected and reported | Pass |
| Add product to cart | Item appears with correct quantity | Pass |
| Place an order | Order saved to Firestore, confirmation shown | Pass |
| Admin updates order status | Notification received on user device | Pass |
| Open plant care guide offline | Tips loaded from SQLite | Pass |
| Upload product image (admin) | Image stored in Firebase Storage, URL saved to Firestore | Pass |
| Search product by name | Matching products displayed | Pass |

### Performance

Measured on a Samsung Galaxy A32 (Android 13) and a Xiaomi Redmi Note 9 (Android 10) over a standard LTE connection:

- Product listing screens loaded in an average of **2.1 seconds**, within the 3-second requirement.
- Images served from the Glide disk cache rendered in **under 200 ms**.

### User feedback

Informal usability testing with five volunteers rated the interface as clean and modern and navigation as intuitive. The wishlist feature was added as a direct result of feedback, and product and nursery reviews were recorded as future work.

---

## Localization

The interface supports seven languages, selectable at runtime from Settings:

English, Sinhala, Tamil, Finnish, German, Chinese (Simplified), and Japanese.

---

## Roadmap

- **Product and nursery reviews and ratings** to build buyer trust.
- **Delivery tracking** through a local courier API for real-time parcel status.
- **Multi-vendor seller portal** so independent nurseries can register and manage their own listings.
- **iOS version** to extend the platform to iPhone users.
- **Broader offline support** such as caching the product catalogue and cart locally.

---

## Author

**Sushen Santhush**
Full Software Engineering undergraduate, Birmingham City University 

- GitHub: [github.com/sushensanthush](https://github.com/sushensanthush)
- LinkedIn: [linkedin.com/in/sushensanthush](https://www.linkedin.com/in/sushensanthush)

---

<div align="center">
<sub>Plant.LK &copy; 2026. Developed as a personal project.</sub>
</div>
