# Jmoney Wear OS — Agreed Improvements

> **Context**: Personal app, ported from React Native. Feature set is stable. Solo developer. No team, no CI/CD, no planned future features.

---

## Decision Summary

| Phase | Title | Decision | Reason |
|-------|-------|----------|--------|
| D | WorkManager Background Sync | ✅ **DO IT** | Keeps tiles fresh automatically without manual refresh |
| E | Error Recovery | ❌ Drop | Manual sync is sufficient; you'll notice failures and retry manually |
| F | Test Coverage | ❌ Drop | No team, stable features — pure overhead with no personal benefit |
| G | Modularization | ❌ Drop | 78 files, fast builds, solo dev — no benefit at this scale |

---

## Phase D: WorkManager for Tile Auto-Refresh

### Why

Currently **none of the 16 tile services set `setFreshnessIntervalMillis()`**. Tiles only update when:
- The user manually taps the refresh button in the app
- Or `requestUpdate()` is explicitly called after a config change

This means tile data can be **stale for hours**. The fix: WorkManager runs a periodic sync in the background and calls `requestUpdate()` on all data tiles when done.

### What Gets Auto-Updated (Data Tiles)

| Tile | Reason |
|------|--------|
| Remaining For Period | Changes with every transaction |
| Monthly Expense | Changes with every expense |
| Monthly Income | Changes with every income |
| Net Worth | Changes with every transaction |
| Budget 1–4 | Changes with every transaction in budget categories |
| Goal 1–3 | Changes when goal is updated |
| Pay Day | Changes daily |

### What Stays Unchanged (Action Tiles — No Refresh Needed)

| Tile | Reason |
|------|--------|
| Add Expense | Pure action, no data displayed |
| Add Income | Pure action, no data displayed |
| Quick Add | Pure action, no data displayed |
| Quick Actions | Pure action, no data displayed |

### Implementation Plan

#### Files to Create
- `data/work/SyncWorker.kt` — CoroutineWorker that:
  1. Checks network connectivity
  2. Calls existing `SyncRepositoryImpl.syncAll()`
  3. Calls `TileService.getUpdater().requestUpdate()` on all 12 data tile services
  4. Returns `Result.success()` or `Result.retry()`

- `data/work/SyncScheduler.kt` — schedules periodic work:
  - Interval: **2 hours** (balance between freshness and battery)
  - Constraint: `requiresNetwork = true`
  - Uses `ExistingPeriodicWorkPolicy.KEEP` (won't reschedule if already queued)

#### Files to Modify
- `presentation/MainActivity.kt` — call `SyncScheduler.schedule()` on app start (once)
- `app/build.gradle.kts` — add WorkManager dependency

#### Dependency to Add
```kotlin
implementation("androidx.work:work-runtime-ktx:2.9.1")
```

#### Sync Interval
- **2 hours** — fresh enough for a personal finance watch app, battery-friendly on Wear OS
- Still supports manual refresh via the existing refresh button

### Verification
- Open any data tile (e.g., Remaining, Budget)
- Wait ~2 hours OR manually trigger WorkManager via `adb shell am broadcast`
- Tile should automatically show updated data without opening the app

---

## Dropped Phases

### Phase E — Error Recovery ❌
- **Dropped**: You are the only user. If sync fails, you'll see stale data and tap refresh. No need for automated retry or error UI.

### Phase F — Test Coverage ❌
- **Dropped**: Stable, feature-complete personal app. No ongoing changes planned. Test infrastructure costs more to build than it saves.

### Phase G — Modularization ❌
- **Dropped**: Single module with 78 files builds quickly. No team, no parallel work. Multi-module adds complexity with zero benefit at this scale.
