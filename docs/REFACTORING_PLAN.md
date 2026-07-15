# Jmoney Wear OS - Refactoring Plan

## Overview

This document outlines the refactoring strategy for the Jmoney Wear OS application to address code smells, improve maintainability, and prepare for Phase 9 (Tiles) and Phase 10 (Complications) implementation.

## Current Issues

### Code Smells

#### 1. Large Classes
- **DashboardScreen.kt**: 966 lines
  - Contains UI, transaction form logic, quick transactions overlay, category/payee selection
  - Multiple responsibilities in one file
  - Difficult to test and maintain

- **SyncRepositoryImpl.kt**: 593 lines
  - Contains sync logic for 7 different entities
  - Push and pull logic mixed together
  - Difficult to understand entity-specific sync behavior

#### 2. Duplicated Code Patterns
- Card layouts duplicated across Dashboard, Budgets, Goals screens
- Button styles repeated in multiple screens
- Loading indicators duplicated
- Similar state management patterns across ViewModels

#### 3. Embedded Logic
- Transaction form logic embedded in DashboardScreen
- Not reusable for other screens
- Makes DashboardScreen even larger

#### 4. Missing Reusable Components
- No reusable UI component library
- Cards, buttons, loading indicators duplicated
- No consistent component API

#### 5. Missing Numeric Keypad
- Uses RemoteInput (system keyboard) for amount entry
- Not optimized for Wear OS
- Poor UX on small screen

#### 6. No WorkManager Integration
- Sync only manual
- No background sync
- Data may become stale

#### 7. Limited Error Recovery
- Some sync errors only logged
- No retry mechanism
- Poor error feedback to users

#### 8. No Test Coverage
- No unit tests
- No integration tests
- No UI tests

## Refactoring Priorities

### ✅ Completed (Before Phase 9)
1. ✅ Extract transaction form logic from DashboardScreen (Phase A)
2. ✅ Create reusable UI component library (Phase B)
3. ✅ Implement custom numeric keypad (Phase A - Step 5)
4. ✅ Split SyncRepositoryImpl by entity (Phase C)

### Remaining (After Phase 10)
1. ⏳ Implement WorkManager for background sync (Phase D)
2. ⏳ Improve error recovery (Phase E)
3. ⏳ Add test coverage (Phase F)
4. ⏳ Modularize into feature modules (Phase G)

## Refactoring Plan - Remaining Phases

### Phase D: Implement WorkManager for Background Sync

#### Objective
Add WorkManager integration for automatic background synchronization.

#### Current State
- Sync only manual
- User must tap refresh button
- Data may become stale

#### Target State
- Automatic periodic sync
- Configurable sync intervals
- Battery-efficient scheduling
- Manual sync still available

#### Steps

1. **Create Worker Package**
   ```
   data/work/
   └── SyncWorker.kt
   ```

2. **Create SyncWorker**
   - Extend CoroutineWorker
   - Check network connectivity
   - Run partial sync
   - Handle errors gracefully
   - Return success/failure

3. **Create SyncScheduler**
   - Create SyncScheduler class
   - Enqueue periodic sync work
   - Support different intervals (hourly, daily)
   - Cancel existing work
   - Check work status

4. **Update SyncRepository**
   - Add sync interval configuration
   - Integrate with SyncScheduler
   - Expose sync scheduling methods

5. **Update Settings**
   - Add sync interval option
   - Add enable/disable background sync
   - Manual trigger option

6. **Update MainActivity**
   - Initialize SyncScheduler on app start
   - Request necessary permissions (if any)

#### Files to Create
- `app/src/main/java/com/jayanth/jmoney/wear/data/work/SyncWorker.kt`
- `app/src/main/java/com/jayanth/jmoney/wear/data/work/SyncScheduler.kt`

#### Files to Modify
- `app/src/main/java/com/jayanth/jmoney/wear/data/repository/SyncRepository.kt`
- `app/src/main/java/com/jayanth/jmoney/wear/data/repository/SyncRepositoryImpl.kt`
- `app/src/main/java/com/jayanth/jmoney/wear/presentation/settings/SettingsScreen.kt`
- `app/src/main/java/com/jayanth/jmoney/wear/presentation/settings/SettingsViewModel.kt`
- `app/src/main/java/com/jayanth/jmoney/wear/presentation/MainActivity.kt`

#### Expected Outcome
- Automatic background sync
- Always up-to-date data
- Better user experience
- Configurable sync intervals

---

### Phase E: Improve Error Recovery

#### Objective
Implement robust error recovery for sync operations.

#### Current State
- Some sync errors only logged
- No retry mechanism
- Poor error feedback to users

#### Target State
- Automatic retry for transient errors
- Clear error messages to users
- Error state tracking
- Manual retry option

#### Steps

1. **Create Error Handler**
   ```
   data/error/
   └── SyncErrorHandler.kt
   ```

2. **Implement SyncErrorHandler**
   - Categorize errors (transient, permanent, network)
   - Determine retry strategy
   - Generate user-friendly messages
   - Track error history

3. **Update SyncRepositoryImpl**
   - Integrate SyncErrorHandler
   - Implement retry logic with exponential backoff
   - Emit detailed error states
   - Track retry attempts

4. **Update SyncViewModel**
   - Display error messages
   - Show retry button
   - Display error history

5. **Update SyncScreen**
   - Show error details
   - Provide retry option
   - Display last successful sync time

#### Files to Create
- `app/src/main/java/com/jayanth/jmoney/wear/data/error/SyncErrorHandler.kt`

#### Files to Modify
- `app/src/main/java/com/jayanth/jmoney/wear/data/repository/SyncRepositoryImpl.kt`
- `app/src/main/java/com/jayanth/jmoney/wear/presentation/sync/SyncViewModel.kt`
- `app/src/main/java/com/jayanth/jmoney/wear/presentation/sync/SyncScreen.kt`

#### Expected Outcome
- Better error handling
- Automatic retry for transient errors
- Clear user feedback
- Improved reliability

---

### Phase F: Add Test Coverage

#### Objective
Add comprehensive test coverage for the application.

#### Current State
- No unit tests
- No integration tests
- No UI tests

#### Target State
- Unit tests for ViewModels
- Unit tests for repositories
- Integration tests for DAOs
- UI tests for critical flows

#### Steps

1. **Create Test Structure**
   ```
   app/src/test/java/com/jayanth/jmoney/wear/
   ├── presentation/
   │   ├── dashboard/
   │   │   └── DashboardViewModelTest.kt
   │   ├── budgets/
   │   │   └── BudgetsViewModelTest.kt
   │   ├── goals/
   │   │   └── GoalsViewModelTest.kt
   │   └── settings/
   │       └── SettingsViewModelTest.kt
   ├── data/
   │   ├── repository/
   │   │   ├── AuthRepositoryImplTest.kt
   │   │   └── SyncRepositoryImplTest.kt
   │   └── database/
   │       └── DaosTest.kt
   └── utils/
       └── FormattersTest.kt

   app/src/androidTest/java/com/jayanth/jmoney/wear/
   ├── presentation/
   │   ├── auth/
   │   │   └── LoginScreenTest.kt
   │   └── dashboard/
   │       └── DashboardScreenTest.kt
   ```

2. **Add Test Dependencies**
   - JUnit 4
   - MockK
   - Kotlin Coroutines Test
   - Room Testing
   - Compose Testing

3. **Write ViewModel Tests**
   - Test state transformations
   - Test action handling
   - Test error scenarios
   - Mock repository dependencies

4. **Write Repository Tests**
   - Test business logic
   - Test error handling
   - Mock database and network

5. **Write DAO Tests**
   - Test database operations
   - Use in-memory database
   - Test queries

6. **Write UI Tests**
   - Test critical user flows
   - Test navigation
   - Test form submission

#### Files to Create
- Multiple test files as outlined above

#### Expected Outcome
- Comprehensive test coverage
- Confidence in code changes
- Regression prevention
- Better code quality

---

### Phase G: Modularize into Feature Modules

#### Objective
Restructure the project into feature-based modules for better scalability.

#### Current State
- Monolithic app module
- All code in single module
- Full rebuild on any change

#### Target State
- Feature-based modules
- Incremental builds
- Clear module boundaries

#### Steps

1. **Create Core Module**
   - Extract theme and utils
   - Create core module build.gradle.kts
   - Update app to depend on core

2. **Create Data Module**
   - Extract database, network, repository
   - Create data module build.gradle.kts
   - Update feature modules to depend on data

3. **Create Common Module**
   - Extract reusable UI components
   - Create common module build.gradle.kts
   - Update feature modules to depend on common

4. **Create Feature Modules**
   - Create feature-auth module
   - Create feature-dashboard module
   - Create feature-budgets module
   - Create feature-goals module
   - Create feature-settings module
   - Create feature-sync module
   - Create feature-quickadd module

5. **Update App Module**
   - Remove extracted code
   - Aggregate feature modules
   - Configure navigation

#### Files to Create
- Multiple module build.gradle.kts files
- Multiple AndroidManifest.xml files

#### Files to Modify
- All existing files moved to appropriate modules
- settings.gradle.kts updated with module declarations

#### Expected Outcome
- Faster builds
- Better code organization
- Clear module boundaries
- Easier team collaboration

---

## Refactoring Timeline

### ✅ Completed (Before Phase 9)
- **Phase A**: Extract Transaction Form Logic (2-3 days) ✅
- **Phase B**: Create Reusable UI Component Library (2-3 days) ✅
- **Phase C**: Split SyncRepositoryImpl by Entity (1-2 days) ✅

### Remaining (After Phase 10)
- **Phase D**: Implement WorkManager for Background Sync (2-3 days)
- **Phase E**: Improve Error Recovery (1-2 days)
- **Phase F**: Add Test Coverage (5-7 days)
- **Phase G**: Modularize into Feature Modules (3-5 days)

## Risk Assessment

### Low Risk (Completed)
- Extracting transaction form logic ✅
- Creating reusable UI components ✅
- Splitting SyncRepositoryImpl ✅

### Medium Risk (Remaining)
- Implementing WorkManager
- Improving error recovery

### High Risk (Remaining)
- Modularizing into feature modules
- Adding comprehensive test coverage

## Rollback Strategy

For each refactoring phase:
1. Create a Git branch before starting
2. Commit frequently with descriptive messages
3. Run tests after each step
4. If issues arise, revert to last working commit
5. Document any breaking changes

## Success Criteria

### Code Quality (Achieved ✅)
- ✅ DashboardScreen ~250 lines (below 500 target)
- ✅ SyncRepositoryImpl ~150 lines (below 300 target)
- ⏳ No code duplication > 10 lines
- ✅ All functions < 50 lines

### Maintainability
- Clear module boundaries
- Consistent coding style
- Comprehensive documentation
- Easy to add new features

### Performance
- No performance regression
- Faster builds (after modularization)
- Better battery efficiency (after WorkManager)

### Testing
- > 80% code coverage
- All critical paths tested
- No flaky tests

## Conclusion

Refactoring Phases A-C are complete. The codebase is now cleaner with:
- Transaction form logic extracted into reusable components
- Custom numeric keypad replacing RemoteInput
- Reusable UI component library created
- Sync logic split into 7 entity-specific classes

The remaining phases (D-G) should be addressed after Phase 10 (Complications) to further improve the application.
