# JmoneyWear Modernization Plan

## Goal

Modernize JmoneyWear in isolated, reviewable phases while preserving existing finance data, offline-first behavior, authentication behavior, and Wear OS functionality. All work is performed on `feature/wear-modernization-2026`; `master` remains untouched until the owner explicitly chooses to merge.

## Non-negotiable constraint: development credentials

**Do not modify the existing production/development credential or auto-login setup.** In particular, do not remove, relocate, rename, replace, or change the behavior of the current `BuildConfig.DEV_EMAIL` / `BuildConfig.DEV_PASSWORD` usage. Do not change credential values or build configuration related to them. This is an explicit owner requirement and overrides recommendations from the previous static review. Other authentication/session code must also remain behavior-compatible unless separately approved.

## Working rules

- Work only on `feature/wear-modernization-2026`.
- Keep changes phase-scoped and commit each completed phase with a descriptive message.
- Do not merge to `master` or create a release without explicit approval.
- Preserve Room data and existing Supabase schema compatibility.
- Never run destructive sync or database operations against real user data during testing.
- Prefer stable releases over alpha/beta dependencies for the release build.
- Record dependency/API decisions and migration notes in this document or linked docs.
- Report what was changed, what was built/tested, and anything the owner must verify on the watch.
- Do not request physical-watch testing or owner sign-off between phases. Complete all implementation phases first; reserve the owner's watch testing for Phase 6 only.
- Do not claim a build or device test passed unless it was actually run.

## Phase 0 — Baseline and planning

**Status: Baseline captured; implementation underway.**

- Record current project versions, module structure, SDK configuration, dependencies, manifest capabilities, and current build/test status.
- Identify all sync flows and data replacement/deletion paths before changing them.
- Capture current screen, tile, navigation, and interaction behavior as a regression baseline.
- Agree on versioning and compatibility strategy.
- No functional code changes in this phase.

**Exit criteria:** baseline and phased plan reviewed; no source behavior changed.

## Phase 1 — Android toolchain and dependency modernization

- Review current stable Android Gradle Plugin, Gradle wrapper, Kotlin, Compose compiler plugin, Compose BOM, Wear Compose Material 3, Room, Hilt, WorkManager, Supabase, Ktor, and Wear Tiles versions.
- Upgrade as a compatible set; do not blindly select the newest version of every library.
- Set compileSdk to the newest stable SDK supported by the chosen Android Gradle Plugin and installed toolchain.
- Set targetSdk according to current Wear OS / Google Play requirements and validate behavior changes before raising it.
- Keep minSdk and supported watch-device range intentional; do not raise minSdk without a compatibility reason.
- Review Java toolchain/JVM target and Gradle configuration.
- Build after each logical upgrade group and resolve deprecations or breaking API changes.
- Update dependency catalog and document compatibility decisions.

**Selected stable baseline (2026-11-08):** Android 17 API 37 (compileSdk/targetSdk 37); Android Gradle Plugin 9.3.1; Gradle 9.7.0; Kotlin 2.4.21; Compose BOM 2026.09.00; Wear Compose libraries 1.7.1; Room 2.8.5; WorkManager 2.12.0; Lifecycle 2.11.0; Wear Tiles 1.6.2; ProtoLayout 1.4.2. Java source/target compatibility is 17. These choices prioritize the published Kotlin 2.4.21 compatibility ceiling (AGP up to 9.3.1 and Gradle up to 9.7.0) while retaining API 37 support. They remain subject to CI verification. The app's minSdk remains 30.

**Implementation status:** dependency catalog, Gradle wrapper, SDK levels, Java compatibility, and version metadata have been upgraded. CI verification is pending. App version is now 2.1.0 (versionCode 7). Existing credential-related code and build configuration were not modified.

**Exit criteria:** clean Gradle sync and successful debug build; dependency matrix documented; no intentional product behavior changes.

## Phase 2 — Sync correctness and data safety

Priority: critical.

- Fix full transaction refresh so local rows are never deleted before a complete, validated remote snapshot has been fetched.
- Apply snapshot replacement atomically in a Room transaction, with safe staging/merge where appropriate.
- Audit Category, Payee, Budget, Goal, Transaction Group, Quick Transaction, and Transaction sync paths.
- Ensure failed uploads are surfaced to the orchestration layer; do not silently swallow an upload failure and then overwrite local pending changes with remote data.
- Preserve pending local edits/deletes until the remote operation is confirmed.
- Serialize sync runs so manual, scheduled, and UI-triggered syncs cannot race.
- Validate partial transaction sync assumptions, especially how remote updates to existing records are detected.
- Review retry/backoff, per-user scoping, sign-out behavior, and immediate sync after local writes.
- Add focused unit/integration tests using fakes or an isolated test database; never use production data for destructive tests.

**Implementation status:** transaction full refresh now fetches and decodes the remote pages before local replacement, preserves pending local edits/tombstones, and refuses suspicious empty snapshots when local data exists. Other entity sync refreshes preserve pending local changes and refuse suspicious empty snapshots. Upload failures now propagate; sync and local reset operations are serialized. Automated verification remains pending.

**Exit criteria:** tests demonstrate that network failures, partial failures, and overlapping sync requests do not lose local changes; normal sync behavior remains intact.

## Phase 3 — Wear OS Material You / Material 3 Expressive redesign

- Keep the Wear-specific `androidx.wear.compose.material3` design system; do not mix mobile Material 3 components/themes into Wear UI.
- Audit all app screens, dialogs, lists, forms, empty states, loading/error states, and tiles for consistent use of Wear Material 3 Expressive.
- Establish a coherent theme: color roles, typography, shapes, spacing, iconography, and component defaults.
- Use dynamic color only where supported and appropriate; maintain a clear fallback palette and sufficient contrast.
- Improve glanceability, touch target sizing, rotary/scroll behavior, curved-screen layout, and one-handed interactions.
- Use expressive motion selectively; keep financial values and key actions calm, legible, and fast.
- Refresh tiles and complications to match the app theme and current Wear OS conventions.
- Preserve all existing finance workflows and avoid introducing unnecessary navigation depth.

**Implementation status:** the theme now uses watch-face dynamic color when available, with the existing brand palette as fallback, and enables Wear Material 3 expressive motion. The app already used Wear Material 3 Expressive components from its previous design migration; a full screen/tile audit and refinements remain.

**Exit criteria:** all key screens and tiles use a coherent Wear-first design; screenshot review across supported emulator sizes where tooling permits; accessibility/contrast and interaction regression checklist completed. Owner's physical-watch testing remains deferred to Phase 6.

## Phase 4 — Architecture, maintainability, and quality

- Replace scattered `printStackTrace()`-only handling with structured logging and user-appropriate error states, without hiding failures.
- Reduce duplicated sync orchestration while keeping entity-specific mapping and rules explicit.
- Enable Room schema export and commit the schema; add tested migrations before any schema change.
- Add tests for repositories, sync edge cases, ViewModels, and important UI behavior.
- Review lifecycle handling, coroutine cancellation, state restoration, and resource use.
- Review manifest permissions and remove only permissions proven unnecessary.
- Improve README with setup, architecture, build, testing, and troubleshooting instructions.

**Implementation status:** added a GitHub Actions workflow for unit tests, lint, debug APK assembly and artifact upload; added initial JUnit tests for formatting helpers. Broader sync tests, Room schema export and migration work remain.

**Exit criteria:** meaningful automated coverage for critical paths, schema/migration baseline committed, and no known high-risk static-review findings left unexplained.

## Phase 5 — Wear OS UX, performance, and reliability

- Review startup time, memory, battery use, background sync behavior, and tile update frequency.
- Verify navigation, scroll-to-top controls, loading states, offline mode, empty data, and error recovery.
- Verify small/large round watch layouts and font scaling.
- Check WorkManager constraints, retries, and duplicate scheduling.
- Ensure tile services and update helpers behave consistently after app/data changes.

**Exit criteria:** automated/emulator-based watch-focused regression checks pass where tooling permits and no major performance or lifecycle regressions are found. Physical watch verification is deferred to Phase 6.

## Phase 6 — Release candidate and owner testing

- Confirm app versionCode/versionName increment strategy and update release metadata.
- Produce a debug APK for owner testing first; produce a release artifact only when requested/appropriate.
- Run available unit tests, lint/static checks, and a clean build.
- Provide APK, commit SHA, change summary, known limitations, and a concise manual test checklist.
- Owner tests on their physical Wear OS watch and reports issues; fixes are made on this feature branch.
- No merge to `master` until the owner approves the tested result.

**Exit criteria:** owner confirms the build is acceptable and explicitly authorizes merge/release.

## Current known risks carried into the plan

1. Transaction full refresh currently deletes local user transactions before remote retrieval is complete.
2. Several entity sync push methods catch and print errors; a subsequent remote refresh may replace local pending edits.
3. Sync orchestration does not currently serialize all concurrent sync runs.
4. Room database is at version 1 with schema export disabled.
5. README and automated test coverage are currently very limited or absent in the inspected tree.

## Progress log

- 2026-11-08: Created `feature/wear-modernization-2026` from `master`.
- 2026-11-08: Added this phased modernization plan. No application source code, production credentials, or runtime behavior changed.
- 2026-11-08: Owner confirmed implementation should proceed through all phases without intermediate physical-watch testing. Physical-watch testing is reserved for the final consolidated release-candidate stage.
- 2026-11-08: Phase 1 implementation started: AGP 9.3.1, Gradle 9.7.0, Kotlin 2.4.21, Compose BOM 2026.09.00, Wear Compose 1.7.1, Room 2.8.5, WorkManager 2.12.0, Lifecycle 2.11.0, Wear Tiles 1.6.2, ProtoLayout 1.4.2, compileSdk/targetSdk 37, Java 17, version 2.1.0 (code 7). CI build verification pending.
- 2026-11-08: Phase 2 source changes committed for safer remote refresh, pending-change preservation, failure propagation, empty-snapshot guards, and sync serialization. Automated verification pending.
- 2026-11-08: Phase 3 initial theme update committed for watch-face dynamic colors and expressive motion. Full UI audit remains.
- 2026-11-08: Added GitHub Actions Android CI workflow and initial JUnit formatting tests. Initial CI attempt exposed the removed legacy SDK `tools` package in setup-android@v3; workflow was corrected to setup-android@v4 with platform-tools only.
- 2026-11-08: Adjusted AGP/Gradle from the newest available line to AGP 9.3.1 + Gradle 9.7.0 to stay within Kotlin 2.4.21's published fully-supported compatibility matrix. Final CI verification pending.
