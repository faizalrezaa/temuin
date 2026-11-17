# Temuin - Project State

Updated: 2026-11-17
Inspected baseline: `feat/fungsi-4-ui` at `032532e`. Review/fixes branch: `fix/match-review-demo`, based on that commit. Comparison base: fetched `origin/main` at `18eebb6`. Local `main` is older and must not be used as the comparison baseline.

Repository source and current check results take precedence over historical memory. These findings replace the 2026-11-16 inventory, which predates integration of the main branch.

## Architecture verified from source

- Single Gradle application module `:app`, namespace/application ID `com.example.temuin`, min SDK 24, compile/target SDK 37.
- Kotlin, Compose, Material 3, Navigation Compose. `MainActivity` initializes `TokenStore`, installs the Coil image client, and renders `TemuinTheme { AppNavigation() }`.
- Feature packages: auth, home, detail, report, peta, inbox, profile, match. ViewModels expose StateFlow, screens collect state and send events. Most screens accept ViewModels, matching additionally uses a stateless screen and pure `MatchPresenter`. There is no separate domain module or DI framework.
- Repository interfaces in `data/repository`, implementations in `data/source/remote`, DTOs/mappers in `data/remote`. `ProfileRepository` is a concrete class. Matching has its own models/interface in `data/match` and shares `TemuinApi`/`ApiClient`.
- Dependencies are wired through manual ViewModel factories, constructors, and singleton clients. Fake repositories remain for previews/development, but production navigation uses remote implementations.
- Ten destinations: login, signup, home with optional location filter, peta, lapor, inbox, profil, edit_profile, item_detail/{itemId}, matches/{reportId}. Startup chooses Home when a stored token exists, otherwise Login. Token presence alone is not proof of a valid session.
- `ApiClient`: Retrofit/Gson over OkHttp, JSON/Bearer/ngrok headers, 20-second connect and 30-second read timeouts. Clears token on non-auth 401. `NetworkModule` separately builds the auth client with BODY logging. Coil uses another client that rewrites backend loopback image hosts.
- Base URL is a hardcoded HTTPS ngrok endpoint in `ApiConfig`, not BuildConfig/flavor configuration. No direct Firebase, Gemini SDK, Room, Hilt/Koin, CameraX, or Google Maps dependency.
- `TokenManager` persists the shared token through EncryptedSharedPreferences/MasterKey. Login uses identifier/password and registration sends name, NIM, faculty, WhatsApp and password confirmation.

## Implemented code paths

- Authentication: login/register API calls, client form validation, saved token, authenticated startup and local logout.
- Feed: remote reports/categories, category/status filters, local search/type/location filtering, refresh and report detail navigation. The old ReportFeedScreen/ReportFixtures route is no longer the active implementation.
- Detail: remote item, secret-description claim submission and WhatsApp contact. Review fix exposes matching on owned reports without exposing self-claim/contact actions.
- Reporting: lost/found form, photo selection, first-photo upload (5 MB limit), remote report creation and pickup options. Category is guessed from title/description, not a fully manual category/color entry. Pos selection produces AWAITING_DROPOFF, not confirmed deposit.
- Map: MapLibre native MapView through AndroidView, OpenFreeMap style, remote pickup points, point search, item sheet, detail and Home location-filter navigation. `getItemsAtPickupPoint` also includes FOUND reports linked by location/building or pickup ID, so it is broader than deposited inventory. Do not claim strict deposited-only availability.
- Inbox: remote incoming/mine claims, approve/reject, refresh, status display, WhatsApp link. This is not a push-notification implementation.
- Profile: remote profile/my reports, edit profile, avatar upload, change password and local logout.
- Matching: POST reports/{id}/matches through the shared authenticated API client, DTO mapping, source/level/attribute/date labels, loading/success/empty/errors, retry and manual-search navigation. Backend performs matching, not Android-local Gemini inference.
- Review fixes cancel superseded matching loads, scope ViewModel by report ID, filter blank-ID/title and duplicate candidates, open candidate details, and provide login recovery for 401. Debug harness visibly labels fixture data and applies system-bar padding.
- No client API methods for report edit/delete, deposit confirmation or handover completion were found. Full planned CRUD/handover/push features must not be described as finished.

## Declared toolchain/dependencies

- Gradle wrapper 9.5.0, AGP 9.3.3, Kotlin Compose plugin 2.2.10, Compose BOM 2026.02.01.
- Core KTX 1.19.1, activity-compose 1.13.0, lifecycle-runtime-ktx 2.11.0, lifecycle-viewmodel-ktx/compose 2.8.7, Navigation Compose 2.7.7 (one declaration), material-icons-extended.
- Retrofit/converter-gson 2.9.0, OkHttp/logging 4.12.0, security-crypto 1.1.0-alpha06, coroutines-android 1.7.3, Coil Compose 2.6.0, MapLibre 11.8.8, graphics-path 1.1.0.
- JUnit 4.13.2, AndroidX test ext 1.3.0, Espresso 3.7.0, Compose UI test/tooling dependencies. No explicit org.json test dependency remains.
- Java source/target 11. Gradle daemon JVM criteria request Java 25, observed using the installed Adoptium 25 JDK. Shell default Java 27 is distinct. Configuration cache is enabled.
- Main manifest declares Internet/fine/coarse location and `usesCleartextTraffic=true`. Debug adds networkSecurityConfig with loopback allowlist and a second launcher `MatchDemoActivity`. Previous claims that release cleartext is disabled are outdated.

## Verification

- 2026-11-17 baseline `:app:testDebugUnitTest :app:assembleDebug`: successful, but up-to-date/cached.
- Fresh `:app:testDebugUnitTest :app:assembleDebug :app:lintDebug --rerun-tasks`: 30 JVM tests passed (1 example, 15 presenter, 9 ViewModel, 5 remote repository). APK assembled. Overall invocation failed because lint crashed inside ExperimentalDetector on MatchDemoActivity with `Unexpected owner function: null`. Lint did not pass and no detector was suppressed.
- Final debug APK and Android test APK compiled. Direct AndroidJUnitRunner instrumentation passed 4/4 tests, including three new matching UI tests. The first Gradle connected run failed at APK installation with insufficient emulator storage, so no tests ran in that invocation. See HANDOFF.md for the retry outcome.
- Unauthenticated HTTPS categories probe returned 401, demonstrating endpoint reachability only. No account credentials were used and no authenticated backend/Gemini behavior is claimed.
- Historical emulator/stub results in older notes are not fresh verification of this checkout.
