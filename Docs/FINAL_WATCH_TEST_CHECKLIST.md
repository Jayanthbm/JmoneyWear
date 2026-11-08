# JmoneyWear — Final Wear OS Emulator Test Checklist

Use this checklist only after all modernization phases are complete and a consolidated debug APK is provided. Run it on a Wear OS emulator before considering a physical-watch test.

## Before installing

- [ ] Confirm the APK version is 2.1.0 / versionCode 7 or newer.
- [ ] Confirm the current app has completed a successful sync.
- [ ] Do not uninstall the existing app to work around a signing mismatch; uninstalling can erase local-only Room data.
- [ ] For an in-place update, build locally with the same signing key used by the currently installed app. A GitHub Actions debug APK is signed with the runner's temporary debug key and may not update an existing local debug installation.

## Install and account

- [ ] Install as an in-place update without clearing app data.
- [ ] Confirm the existing account/session is still recognized.
- [ ] Confirm the existing development auto-login behavior remains unchanged.
- [ ] Confirm dashboard values and existing transaction history are present.

## Navigation and Material You design

- [ ] Swipe through Dashboard, Transactions, Budgets, Goals, and Settings.
- [ ] Confirm the new horizontal page indicator tracks the active page.
- [ ] Confirm page transitions are smooth and do not interfere with overlays or dialogs.
- [ ] Change the watch-face color palette and verify the app adopts dynamic colors where supported.
- [ ] Verify the fallback brand palette remains legible when dynamic colors are unavailable.
- [ ] Check text clipping, contrast, touch targets, and curved-edge spacing.
- [ ] Test with larger system font/display settings if available.

## Finance workflows

- [ ] Add an expense and confirm it appears in Transactions.
- [ ] Add an income and confirm it appears in Transactions.
- [ ] Verify Indian currency grouping and decimal formatting.
- [ ] Verify transaction descriptions, payees, and categories display correctly.
- [ ] Open Budgets and verify progress/overspending states.
- [ ] Open Goals and verify progress/completion states.
- [ ] Verify quick transaction actions and dashboard metrics.

## Tiles

- [ ] Add each supported JmoneyWear tile from the emulator's tile editor.
- [ ] Confirm tiles render without blank states or clipped content.
- [ ] Add a transaction and sync; verify affected tiles refresh.
- [ ] Verify action-only tiles still launch the intended app action.
- [ ] Remove and re-add a tile; verify it recovers correctly.

## Offline and synchronization safety

- [ ] Enable Airplane mode.
- [ ] Add an expense and confirm it remains visible locally.
- [ ] Trigger sync while offline; confirm the app reports failure/retry without clearing local data.
- [ ] Re-enable connectivity and run sync.
- [ ] Confirm the offline transaction appears remotely and does not duplicate locally.
- [ ] Verify existing transactions, budgets, goals, payees, categories, and quick presets remain present.
- [ ] Repeat a sync and confirm it is idempotent.
- [ ] If a sync error occurs, do not use Reset Local Data; capture the error and report it.

## Reliability and performance

- [ ] Navigate repeatedly between all pages.
- [ ] Scroll long transaction/budget/goal lists and use scroll-to-top controls.
- [ ] Verify the emulator remains responsive while background sync is running.
- [ ] Confirm no unexpected app crashes, freezes, or battery drain.
- [ ] Leave the app in the background and verify scheduled sync does not disrupt normal watch use.

## Report back

Include the APK version, emulator device profile, Wear OS version, steps performed, expected result, actual result, and screenshots/video for any visual or functional issue.

## Emulator setup notes

- [ ] Use a round Wear OS emulator profile representative of the target watch size; also try a second screen size if available.
- [ ] Keep the existing emulator app data when testing an in-place upgrade. Do not clear app storage or uninstall to bypass an install/signature issue.
- [ ] Capture Logcat around tile host launches, cold starts, authentication restoration, and sync failures.
- [ ] If the CI debug APK cannot update the installed build because its debug signing key differs, install it only on a disposable emulator profile or build locally with the matching signing key; do not delete the user's existing app data.
