# JmoneyWear

JmoneyWear is the Wear OS companion app for quick personal-finance tracking from a smartwatch. It provides an on-watch dashboard, transaction entry, budgets, goals, quick transaction presets, and Wear OS tiles, with local Room storage and Supabase synchronization.

## Project status

- Application ID: `com.jayanth.jmoney.wear`
- Current modernization branch: `feature/wear-modernization-2026`
- Release line: `2.1.0` (versionCode `7`)
- Minimum SDK: API 30
- Compile SDK / target SDK: API 37 (Android 17)
- Java toolchain/source compatibility: 17
- UI: Jetpack Compose for Wear OS with Wear Material 3 Expressive
- Local persistence: Room
- Remote data/auth: Supabase Kotlin
- Background sync: WorkManager
- Dependency injection: Hilt

The modernization plan and phase progress are tracked in [Docs/MODERNIZATION_PLAN.md](Docs/MODERNIZATION_PLAN.md).

## Main capabilities

- Dashboard with remaining-for-period, today's spending, pay-day, monthly income/expense, and net-worth summaries.
- Add income and expense transactions directly from the watch.
- Quick transaction presets.
- Browse transactions, budgets, and goals.
- Wear OS tiles for key finance metrics and quick actions.
- Offline-first local data using Room, with Supabase synchronization when connected.
- Watch-face dynamic Material You colors when supported, with a branded fallback palette.

## Architecture overview

```text
Wear Compose UI
    |
    v
ViewModels / Repositories
    |
    +---- Room database (local-first reads and writes)
    |
    +---- Supabase (remote authentication and synchronization)
    |
    +---- WorkManager (network-constrained background sync)
    |
    +---- Wear OS Tiles (glanceable finance summaries and actions)
```

Sync is orchestrated by `SyncRepositoryImpl`; entity-specific mapping and remote operations live under `data/sync`. Room entities and DAOs are under `data/database`. Wear UI, themes, and tile services are under `presentation`.

## Development setup

1. Install Android Studio with Android SDK Platform 37 and Build Tools 37.
2. Install/use JDK 17.
3. Clone the repository and check out the desired branch.
4. Keep machine-specific configuration in the ignored `local.properties` file at the project root.
5. Open the project in Android Studio or use the Gradle wrapper.

The existing development auto-login credential configuration is intentionally preserved. The app build currently reads `dev.email` and `dev.password` from `local.properties` and exposes them through the existing `BuildConfig.DEV_EMAIL` and `BuildConfig.DEV_PASSWORD` fields. This modernization work does not remove or change that behavior. Do not commit `local.properties` or real signing keystores.

Release signing properties, when used, are also read from local machine configuration. Do not place passwords or keystore files in source control.

## Build and verification

Run from the repository root:

```bash
./gradlew --no-daemon testDebugUnitTest
./gradlew --no-daemon lintDebug
./gradlew --no-daemon assembleDebug
```

Combined CI command:

```bash
./gradlew --no-daemon testDebugUnitTest lintDebug assembleDebug
```

Debug APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

A GitHub Actions workflow at `.github/workflows/android-ci.yml` runs unit tests, lint, and debug APK assembly on pushes to the modernization branch. Successful runs upload a short-lived debug APK artifact.

## Versioning

The version is controlled by `app.versionCode` and `app.versionName` in `gradle.properties`. Increment versionCode for every distributable build; update versionName according to the release scope.

## Data safety notes

- Preserve local Room data during network errors and incomplete remote reads.
- A failed local upload must stop the refresh sequence rather than allowing remote data to overwrite pending local edits.
- Never use a production account/database for destructive sync or migration experiments.
- Database schema changes require a version increment and a tested Room migration; do not use destructive migration fallbacks.
- Full physical-watch regression testing is reserved for the final consolidated release candidate in the modernization workflow.

## CI artifacts

Open the repository's **Actions → Android CI** workflow, select a successful run, and download the `jmoneywear-debug-apk` artifact. Artifacts are retained for 14 days. A GitHub Actions debug APK is signed with the runner's temporary debug key and may not install as an update over an APK signed with your local debug key. For a data-preserving in-place update, build locally using the same signing key as the installed app. Do not uninstall the existing app merely to work around a signature mismatch, because that can erase local-only Room data.
