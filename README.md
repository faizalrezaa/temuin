# Temuin

**Campus lost-and-found, connected to a map and AI-assisted matching.**

![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-min%20SDK%2024-3DDC84?logo=android&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)

Temuin is an Android app for reporting and finding belongings around campus. It brings reports, pickup locations, item claims, and help into one place. AI recommendations compare eligible lost and found reports, and show the similarities and differences that informed each result.

[Features](#features) · [Technology](#technology) · [Getting started](#getting-started) · [Using the app](#using-the-app) · [Project structure](#project-structure) · [Testing](#testing) · [Roadmap](#roadmap) · [Contributing](#contributing) · [License](#license)

## About the project

Lost-and-found notices are often scattered across chats and social media. Temuin gives students a structured way to publish a report, search current listings, and follow up through the app.

### Features

- **Sign in and registration** with account details managed by the Temuin API.
- **Report feed** with keyword search, status and category filters, item photos, and report details.
- **Lost and found reports** with descriptions, location, time, and optional photos.
- **Campus map** with pickup locations and links from map results to report details.
- **AI-assisted matching** from a lost report, with candidate photos, match attributes, and an explanation of similarities and differences.
- **Bantuan Temuin** for feature guidance and questions about active public reports. Matching and chat use DeepSeek through SinarCloud when the backend is configured for it.
- **Claims and inbox** for submitting an ownership claim and following its status.
- **Profile** with editable account information, avatar, and the user's reports.

AI matching is a recommendation to help review reports. Users should verify an item's distinctive characteristics before claiming it.

## Technology

| Area | Technology |
| --- | --- |
| App and interface | Kotlin, Jetpack Compose, Material 3 |
| Navigation and state | Navigation Compose, AndroidX ViewModel, Kotlin Flow |
| API client | Retrofit, Gson, OkHttp |
| Images | Coil |
| Campus map | MapLibre with OpenFreeMap styles |
| Backend | Go, Fiber, GORM, PostgreSQL |
| AI provider | DeepSeek through SinarCloud, configured on the backend |

The Android client is in this repository. The API is maintained separately in [`haramurti/temuin-be`](https://github.com/haramurti/temuin-be). The Android app does not contain provider credentials.

## Getting started

### Prerequisites

- Android Studio with Android SDK 37 installed.
- JDK 25 for the Gradle daemon configured by this project.
- Docker with Docker Compose for running the API and PostgreSQL locally.
- An Android emulator or a connected Android device.
- A SinarCloud API key on the backend for live DeepSeek answers. The app can also use the backend's rule-based matching and help fallbacks without it.

### Set up the backend

In a terminal, clone the backend repository and follow its configuration instructions:

```bash
git clone https://github.com/haramurti/temuin-be.git
cd temuin-be
cp .env.example .env
```

Set `SINARCLOUD_API_KEY` in the backend `.env` to enable DeepSeek. Keep this key on the backend and out of the Android project. Start the API and PostgreSQL with Docker Compose:

```bash
make up
```

The API health endpoint is `http://localhost:8080/health`. The backend README documents additional local and remote setup options.

### Set up Android

Clone this repository and open it in Android Studio:

```bash
git clone https://github.com/faizalrezaa/temuin.git
cd temuin
```

For an emulator using the local backend, set this Gradle property in `~/.gradle/gradle.properties`:

```properties
temuinDebugApiUrl=http://10.0.2.2:8080/api/v1/
```

`10.0.2.2` lets the Android emulator reach services on the development computer. Android Studio then uses this URL for the debug build. For a different API environment, set `temuinDebugApiUrl` to an HTTPS API base URL ending in `/api/v1/`.

Sync Gradle, choose the `app` run configuration and an emulator, then select **Run**. To build from a terminal:

```bash
./gradlew :app:assembleDebug
```

If you prefer a one-time local override, pass the property directly:

```bash
./gradlew :app:assembleDebug -PtemuinDebugApiUrl=http://10.0.2.2:8080/api/v1/
```

Release builds use the HTTPS API endpoint configured in `app/build.gradle.kts`. Update that configuration for the release environment before distributing a build.

## Using the app

1. Sign in or create an account.
2. Browse the feed or use search and filters to find reports.
3. Open a report to inspect its photo, details, and available actions.
4. Submit a lost or found report from **Lapor**.
5. Open **Cari Kecocokan** from a lost report to review matching candidates and the AI explanation.
6. Use **Bantuan AI Temuin** from Beranda to ask about the app or search for active public reports.
7. Follow claim updates in **Inbox** and view campus pickup locations on **Peta**.

## Project structure

```text
app/src/main/java/com/example/temuin/
├── data/          API models, repositories, local token storage
├── navigation/    app routes and repository wiring
└── ui/            Compose screens and ViewModels by feature
    ├── auth/
    ├── detail/
    ├── help/
    ├── home/
    ├── inbox/
    ├── match/
    ├── peta/
    ├── profile/
    └── report/
```

The app uses one Gradle module, `:app`. Retrofit services and response models live in `data/remote`; feature repositories map API data for their ViewModels and Compose screens. Repository implementations and navigation wiring are kept in the Android project rather than separate Gradle modules.

## Testing

Run the local unit tests and lint checks with Gradle:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

With an emulator running, run Android instrumented tests:

```bash
./gradlew :app:connectedDebugAndroidTest
```

For local API-backed checks, start PostgreSQL and the backend first, then run the app with `temuinDebugApiUrl` set to the backend URL.

## Roadmap

- Add CI checks for Android builds, unit tests, and lint.
- Provide documented configuration for development, staging, and release API environments.
- Expand automated coverage for authenticated API-backed user flows.

## Contributing

Contributions and bug reports are welcome. Before opening a pull request:

1. Create a focused branch from the current project branch.
2. Keep changes scoped to one feature or fix where practical.
3. Run the relevant Gradle checks and describe their results.
4. Include clear reproduction steps for bug fixes and screenshots for visible UI changes.
5. Open a pull request with a concise summary and any required backend changes.

## License

This repository does not currently include a `LICENSE` file.

## Contact

For questions or bug reports, open an issue in the [Temuin Android repository](https://github.com/faizalrezaa/temuin). For API changes, use the [Temuin backend repository](https://github.com/haramurti/temuin-be).

## Acknowledgments

- [Best-README-Template](https://github.com/othneildrew/Best-README-Template) for the README section structure.
- [OpenStreetMap](https://www.openstreetmap.org/copyright), [MapLibre](https://maplibre.org/), and [OpenFreeMap](https://openfreemap.org/) for the map stack.
- [SinarCloud](https://docs.sinarcloud.com/) for the configured DeepSeek API provider.
