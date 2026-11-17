# Fungsi 4 demo

Open `/home/giptarch/Projects/temuin` in Android Studio. Keep the editor visible beside **View > Tool Windows > Running Devices**. Use the existing `medium_phone` emulator. If it does not appear, select it in Device Manager. Do not start a second emulator for the same AVD.

## Prepare

```sh
./gradlew :app:testDebugUnitTest :app:assembleDebug
adb -s emulator-5554 install -r app/build/outputs/apk/debug/app-debug.apk
```

The terminal needs the Android SDK `platform-tools` directory on PATH. Android Studio can run the `app` configuration directly instead of using `adb install`.

## Live application: approximately 3 minutes

1. Run `MainActivity`. Sign in with an existing team test account. No credentials belong in this document or video.
2. Open **Profil**, select your own active report, then tap **Cari Kecocokan**. A report from Beranda also provides this entry point.
3. Explain the code path while showing these files:
   - `navigation/AppNavigation.kt`: passes the selected report ID to `matches/{reportId}` and supplies navigation callbacks.
   - `ui/match/MatchViewModel.kt`: loading state, cancellation of an older request, retry, and error mapping.
   - `data/match/RemoteMatchRepository.kt`: calls `TemuinApi.matches`, maps DTOs, filters unusable/duplicate report IDs, and preserves coroutine cancellation.
   - `data/remote/ApiClient.kt`: shared Retrofit/OkHttp client attaches the saved Bearer token.
   - `ui/match/MatchPresenter.kt`: Indonesian labels, dates, and user-facing error states.
   - `ui/match/MatchRecommendationScreen.kt`: renders loading, success, empty, and error states from caller-supplied data.
4. Show the source label, match level, explanation, and matching/different/uncomparable attributes. Tap **Lihat Laporan** on a candidate. It opens the normal report detail, where existing claim/contact rules apply. Back returns to recommendations.
5. Explain that recommendations help discovery. Claim approval remains a separate process. The Android code calls the matching backend, it does not invoke Gemini directly.
6. If the session expires, demonstrate **Masuk Kembali**. If the service fails or returns no candidates, use retry/manual search as offered by that state.

A successful live demo requires a working backend, a valid account, and reports that generate candidates. The configured ngrok URL is in `ApiConfig.BASE_URL`. A response of HTTP 401 from an unauthenticated probe proves reachability only. It does not prove that auth, recommendations, or Gemini work.

## Offline UI demo: approximately 1 minute

```sh
adb -s emulator-5554 shell am start -n com.example.temuin/.debug.MatchDemoActivity
```

Alternatively, open the **Temuin - Demo Fungsi 4** launcher on the emulator. This debug-only screen is labeled **Demo UI · data simulasi**. Select AI, Aturan, Data kurang, Kosong, Loading, and Gagal 429 in the horizontal chip row. Loading changes to AI after 1.2 seconds. Coba Lagi returns to simulated success. Cari Manual shows a message because the harness does not connect to the application feed.

This demonstrates UI states with fixtures. It does not test authentication, HTTP requests, real recommendations, or server-side Gemini. Use MainActivity for navigation to candidate details and authentication recovery.

## Checks

```sh
./gradlew :app:testDebugUnitTest
ANDROID_SERIAL=emulator-5554 ./gradlew :app:connectedDebugAndroidTest
./gradlew :app:assembleDebug
./gradlew :app:lintDebug
```

On 2026-11-17, lint crashed in `ExperimentalDetector` while analyzing `MatchDemoActivity.kt` with `Unexpected owner function: null`. No detector suppression was added. See `.agent-memory/HANDOFF.md` for the latest check outcomes.

The existing emulator has limited /data capacity. Gradle connected tests hit INSTALL_FAILED_INSUFFICIENT_STORAGE. Both APKs installed successfully through adb install -r, and direct instrumentation passed all four tests:

```sh
adb -s emulator-5554 install -r app/build/outputs/apk/debug/app-debug.apk
adb -s emulator-5554 install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s emulator-5554 shell am instrument -w com.example.temuin.test/androidx.test.runner.AndroidJUnitRunner
```
