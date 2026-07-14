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

### High Priority (Before Phase 9)
1. Extract transaction form logic from DashboardScreen
2. Create reusable UI component library
3. Implement custom numeric keypad
4. Split SyncRepositoryImpl by entity

### Medium Priority (After Phase 10)
1. Modularize into feature modules
2. Add WorkManager for background sync
3. Improve error recovery
4. Add test coverage

### Low Priority (Future)
1. Performance optimizations
2. Caching strategy
3. Predictive pre-fetching
4. Analytics integration

## Refactoring Plan

### Phase A: Extract Transaction Form Logic

#### Objective
Extract transaction form logic from DashboardScreen into a reusable component.

#### Current State
- Transaction form embedded in DashboardScreen (lines 626-966)
- Category/payee selection overlays in DashboardScreen
- RemoteInput for amount/description
- Not reusable

#### Target State
- Separate TransactionForm composable
- Separate CategorySelector composable
- Separate PayeeSelector composable
- Reusable across screens
- Custom numeric keypad

#### Steps

1. **Create Form Components Package**
   ```
   presentation/form/
   ├── TransactionForm.kt
   ├── CategorySelector.kt
   ├── PayeeSelector.kt
   └── NumericKeypad.kt
   ```

2. **Extract TransactionForm**
   - Create `TransactionForm.kt` composable
   - Accept parameters: amount, type, category, payee, description
   - Accept callbacks: onConfirm, onCancel
   - Use custom numeric keypad for amount entry
   - Use CategorySelector and PayeeSelector

3. **Extract CategorySelector**
   - Create `CategorySelector.kt` composable
   - Accept parameters: categories, selectedCategory
   - Accept callback: onCategorySelected
   - Display as overlay with TransformingLazyColumn

4. **Extract PayeeSelector**
   - Create `PayeeSelector.kt` composable
   - Accept parameters: payees, selectedPayee
   - Accept callback: onPayeeSelected
   - Display as overlay with TransformingLazyColumn

5. **Implement NumericKeypad**
   - Create `NumericKeypad.kt` composable
   - Calculator-style layout (7-8-9, 4-5-6, 1-2-3, .-0-⌫)
   - Large touch targets for watch
   - Decimal support
   - Clear and delete functionality
   - Rotary input compatibility
   - Replace RemoteInput in TransactionForm

6. **Update DashboardScreen**
   - Replace embedded form with TransactionForm
   - Simplify to ~400 lines
   - Focus on dashboard-specific UI

7. **Update DashboardViewModel**
   - Move transaction-specific state to TransactionForm
   - Keep only dashboard-specific state

#### Files to Create
- `app/src/main/java/com/jayanth/jmoney/wear/presentation/form/TransactionForm.kt`
- `app/src/main/java/com/jayanth/jmoney/wear/presentation/form/CategorySelector.kt`
- `app/src/main/java/com/jayanth/jmoney/wear/presentation/form/PayeeSelector.kt`
- `app/src/main/java/com/jayanth/jmoney/wear/presentation/form/NumericKeypad.kt`

#### Files to Modify
- `app/src/main/java/com/jayanth/jmoney/wear/presentation/dashboard/DashboardScreen.kt`
- `app/src/main/java/com/jayanth/jmoney/wear/presentation/dashboard/DashboardViewModel.kt`

#### Expected Outcome
- DashboardScreen reduced from 966 to ~400 lines
- Transaction form reusable for future features
- Better UX with custom numeric keypad
- Cleaner separation of concerns

---

### Phase B: Create Reusable UI Component Library

#### Objective
Create a library of reusable UI components to eliminate code duplication.

#### Current State
- Card layouts duplicated across screens
- Button styles repeated
- Loading indicators duplicated
- No consistent component API

#### Target State
- Common component library in `presentation/common/`
- Consistent API across components
- Reusable across all screens
- Documented usage patterns

#### Steps

1. **Create Common Components Package**
   ```
   presentation/common/
   ├── cards/
   │   ├── InfoCard.kt
   │   ├── MetricCard.kt
   │   └── TransactionCard.kt
   ├── buttons/
   │   ├── ActionButton.kt
   │   └── IconButton.kt
   ├── loading/
   │   └── LoadingIndicator.kt
   └── overlays/
       └── FullScreenOverlay.kt
   ```

2. **Extract InfoCard**
   - Create reusable InfoCard component
   - Accept parameters: title, value, color, onClick
   - Used for dashboard summary cards
   - Replace in DashboardScreen

3. **Extract MetricCard**
   - Create reusable MetricCard component
   - Accept parameters: label, value, unit, progress
   - Used for budget/goal cards
   - Replace in BudgetsScreen and GoalsScreen

4. **Extract TransactionCard**
   - Create reusable TransactionCard component
   - Accept parameters: transaction, onClick
   - Used for transaction lists
   - Replace in DashboardScreen

5. **Extract ActionButton**
   - Create reusable ActionButton component
   - Accept parameters: icon, onClick, colors
   - Used for quick action buttons
   - Replace in DashboardScreen

6. **Extract LoadingIndicator**
   - Create reusable LoadingIndicator component
   - Accept parameters: message, isFullWidth
   - Used across all screens
   - Replace duplicated indicators

7. **Extract FullScreenOverlay**
   - Create reusable FullScreenOverlay component
   - Accept parameters: content, onBack
   - Used for overlays
   - Replace overlay patterns

8. **Update Screens**
   - Replace duplicated components with common components
   - Ensure consistent styling
   - Reduce code duplication

#### Files to Create
- `app/src/main/java/com/jayanth/jmoney/wear/presentation/common/cards/InfoCard.kt`
- `app/src/main/java/com/jayanth/jmoney/wear/presentation/common/cards/MetricCard.kt`
- `app/src/main/java/com/jayanth/jmoney/wear/presentation/common/cards/TransactionCard.kt`
- `app/src/main/java/com/jayanth/jmoney/wear/presentation/common/buttons/ActionButton.kt`
- `app/src/main/java/com/jayanth/jmoney/wear/presentation/common/buttons/IconButton.kt`
- `app/src/main/java/com/jayanth/jmoney/wear/presentation/common/loading/LoadingIndicator.kt`
- `app/src/main/java/com/jayanth/jmoney/wear/presentation/common/overlays/FullScreenOverlay.kt`

#### Files to Modify
- `app/src/main/java/com/jayanth/jmoney/wear/presentation/dashboard/DashboardScreen.kt`
- `app/src/main/java/com/jayanth/jmoney/wear/presentation/budgets/BudgetsScreen.kt`
- `app/src/main/java/com/jayanth/jmoney/wear/presentation/goals/GoalsScreen.kt`
- `app/src/main/java/com/jayanth/jmoney/wear/presentation/settings/SettingsScreen.kt`

#### Expected Outcome
- Reduced code duplication
- Consistent UI across screens
- Easier to maintain
- Faster development of new features

---

### Phase C: Split SyncRepositoryImpl by Entity

#### Objective
Split SyncRepositoryImpl into smaller, focused classes per entity.

#### Current State
- SyncRepositoryImpl is 593 lines
- Contains sync logic for 7 entities
- Push and pull logic mixed
- Difficult to understand entity-specific behavior

#### Target State
- Separate sync classes for each entity
- SyncRepositoryImpl orchestrates entity sync
- Clear separation of concerns
- Easier to test and maintain

#### Steps

1. **Create Entity Sync Package**
   ```
   data/repository/sync/
   ├── TransactionSync.kt
   ├── CategorySync.kt
   ├── PayeeSync.kt
   ├── BudgetSync.kt
   ├── GoalSync.kt
   ├── QuickTransactionSync.kt
   └── TransactionGroupSync.kt
   ```

2. **Extract TransactionSync**
   - Create TransactionSync class
   - Move transaction push logic
   - Move transaction pull logic
   - Handle chunked fetching
   - Handle tid-based incremental sync

3. **Extract CategorySync**
   - Create CategorySync class
   - Move category push logic
   - Move category pull logic
   - Handle full replace

4. **Extract PayeeSync**
   - Create PayeeSync class
   - Move payee push logic
   - Move payee pull logic
   - Handle full replace

5. **Extract BudgetSync**
   - Create BudgetSync class
   - Move budget push logic
   - Move budget pull logic
   - Handle JSON serialization for categories

6. **Extract GoalSync**
   - Create GoalSync class
   - Move goal push logic
   - Move goal pull logic
   - Handle full replace

7. **Extract QuickTransactionSync**
   - Create QuickTransactionSync class
   - Move quick transaction push logic
   - Move quick transaction pull logic
   - Handle full replace

8. **Extract TransactionGroupSync**
   - Create TransactionGroupSync class
   - Move group push logic
   - Move group pull logic
   - Handle full replace

9. **Update SyncRepositoryImpl**
   - Use entity sync classes
   - Orchestrate sync sequence
   - Manage sync state
   - Reduce to ~200 lines

#### Files to Create
- `app/src/main/java/com/jayanth/jmoney/wear/data/repository/sync/TransactionSync.kt`
- `app/src/main/java/com/jayanth/jmoney/wear/data/repository/sync/CategorySync.kt`
- `app/src/main/java/com/jayanth/jmoney/wear/data/repository/sync/PayeeSync.kt`
- `app/src/main/java/com/jayanth/jmoney/wear/data/repository/sync/BudgetSync.kt`
- `app/src/main/java/com/jayanth/jmoney/wear/data/repository/sync/GoalSync.kt`
- `app/src/main/java/com/jayanth/jmoney/wear/data/repository/sync/QuickTransactionSync.kt`
- `app/src/main/java/com/jayanth/jmoney/wear/data/repository/sync/TransactionGroupSync.kt`

#### Files to Modify
- `app/src/main/java/com/jayanth/jmoney/wear/data/repository/SyncRepositoryImpl.kt`

#### Expected Outcome
- SyncRepositoryImpl reduced from 593 to ~200 lines
- Clear separation of entity sync logic
- Easier to test individual entity sync
- Easier to add new entities

---

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

### Before Phase 9 (Tiles)
- **Phase A**: Extract Transaction Form Logic (2-3 days)
- **Phase B**: Create Reusable UI Component Library (2-3 days)
- **Phase C**: Split SyncRepositoryImpl by Entity (1-2 days)
- **Phase D**: Implement Custom Numeric Keypad (1-2 days)

### After Phase 10 (Complications)
- **Phase E**: Implement WorkManager for Background Sync (2-3 days)
- **Phase F**: Improve Error Recovery (1-2 days)
- **Phase G**: Add Test Coverage (5-7 days)
- **Phase H**: Modularize into Feature Modules (3-5 days)

## Risk Assessment

### Low Risk
- Extracting transaction form logic
- Creating reusable UI components
- Splitting SyncRepositoryImpl

### Medium Risk
- Implementing WorkManager
- Improving error recovery

### High Risk
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

### Code Quality
- DashboardScreen < 500 lines
- SyncRepositoryImpl < 300 lines
- No code duplication > 10 lines
- All functions < 50 lines

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

This refactoring plan addresses the current code smells and prepares the codebase for Phase 9 (Tiles) and Phase 10 (Complications). The phased approach allows for incremental improvements with minimal risk. High-priority refactoring should be completed before implementing Tiles and Complications to ensure a clean, maintainable codebase.
