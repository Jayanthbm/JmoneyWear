# Jmoney Wear OS - Module Structure Documentation

## Current Package Structure

The project currently uses a monolithic app module with the following package organization:

```
com.jayanth.jmoney.wear/
├── JmoneyApp.kt                          # Application class
├── data/                                 # Data layer
│   ├── database/                         # Room database
│   │   ├── AppDatabase.kt                # Database configuration
│   │   ├── Daos.kt                       # Data Access Objects
│   │   └── Entities.kt                   # Room entities
│   ├── network/                          # Network layer
│   │   └── dto/
│   │       └── SupabaseDto.kt            # Supabase DTOs
│   ├── repository/                      # Repository implementations
│   │   ├── AuthRepository.kt             # Auth interface
│   │   ├── AuthRepositoryImpl.kt         # Auth implementation
│   │   ├── SyncRepository.kt             # Sync interface
│   │   └── SyncRepositoryImpl.kt         # Sync implementation (refactored, ~150 lines)
│   ├── sync/                             # Entity-specific sync classes
│   │   ├── TransactionSync.kt            # Transaction sync logic
│   │   ├── CategorySync.kt               # Category sync logic
│   │   ├── PayeeSync.kt                  # Payee sync logic
│   │   ├── BudgetSync.kt                 # Budget sync logic
│   │   ├── GoalSync.kt                   # Goal sync logic
│   │   ├── QuickTransactionSync.kt       # Quick transaction sync logic
│   │   └── TransactionGroupSync.kt       # Transaction group sync logic
│   └── storage/                          # Secure storage
│       └── SecureStorage.kt             # Encrypted storage
├── di/                                   # Dependency injection
│   ├── DatabaseModule.kt                 # Database DI
│   ├── NetworkModule.kt                  # Network DI
│   └── RepositoryModule.kt               # Repository DI
└── presentation/                         # Presentation layer
    ├── MainActivity.kt                    # Main activity
    ├── auth/                             # Authentication feature
    │   ├── LoginScreen.kt
    │   └── LoginViewModel.kt
    ├── budgets/                          # Budgets feature
    │   ├── BudgetsScreen.kt
    │   └── BudgetsViewModel.kt
    ├── common/                           # Reusable UI component library
    │   ├── buttons/
    │   │   └── ActionButton.kt           # Reusable action button
    │   ├── cards/
    │   │   ├── InfoCard.kt               # General information card
    │   │   ├── MetricCard.kt             # Metric display card
    │   │   └── TransactionCard.kt        # Transaction display card
    │   ├── loading/
    │   │   └── LoadingIndicator.kt       # Loading progress indicator
    │   └── overlays/
    │       └── FullScreenOverlay.kt      # Full screen overlay container
    ├── dashboard/                        # Dashboard feature
    │   ├── DashboardScreen.kt
    │   └── DashboardViewModel.kt
    ├── form/                             # Transaction form components
    │   ├── CategorySelector.kt           # Category selection overlay
    │   ├── NumericKeypad.kt              # Custom numeric keypad
    │   ├── PayeeSelector.kt              # Payee selection overlay
    │   └── TransactionForm.kt            # Transaction form composable
    ├── goals/                            # Goals feature
    │   ├── GoalsScreen.kt
    │   └── GoalsViewModel.kt
    ├── settings/                         # Settings feature
    │   ├── SettingsScreen.kt
    │   └── SettingsViewModel.kt
    ├── sync/                             # Sync feature
    │   ├── SyncScreen.kt
    │   └── SyncViewModel.kt
    ├── tiles/                            # Tiles feature (Phase 9)
    │   ├── RemainingForPeriodTileService.kt # Remaining for period tile service
    │   ├── UpcomingPayDayTileService.kt  # Upcoming pay day tile service
    │   ├── QuickAddExpenseTileService.kt # Quick add expense tile service
    │   ├── QuickAddIncomeTileService.kt  # Quick add income tile service
    │   ├── QuickTransactionTileService.kt # Quick transaction tile service
    │   ├── QuickActionsTileService.kt    # Quick actions combination tile service
    │   ├── MonthlyExpenseTileService.kt  # Monthly expense tile service
    │   ├── tiles/
    │   │   ├── RemainingForPeriodTile.kt # Remaining for period tile layout
    │   │   ├── UpcomingPayDayTile.kt     # Upcoming pay day tile layout
    │   │   ├── QuickAddExpenseTile.kt    # Quick add expense tile layout
    │   │   ├── QuickAddIncomeTile.kt     # Quick add income tile layout
    │   │   ├── QuickTransactionTile.kt   # Quick transaction tile layout
    │   │   ├── QuickActionsTile.kt       # Quick actions combination tile layout
    │   │   └── MonthlyExpenseTile.kt     # Monthly expense tile layout
    │   └── utils/
    │       ├── TileDataFetcher.kt        # Data fetcher for tiles
    │       └── TileResourceBuilder.kt    # Resource builder for tiles
    ├── theme/                            # Theme configuration
    │   ├── Color.kt
    │   └── Theme.kt
    └── utils/                            # Utilities
        └── Formatters.kt
```

## Package Responsibilities

### Root Package (`com.jayanth.jmoney.wear`)
- **JmoneyApp.kt**: Application entry point, Hilt application

### Data Layer (`data/`)

#### Database (`data/database/`)
- **AppDatabase.kt**: Room database configuration, version management
- **Daos.kt**: DAO interfaces for all entities
  - TransactionDao: Transaction queries
  - CategoryDao: Category queries
  - PayeeDao: Payee queries
  - QuickTransactionDao: Quick transaction queries
  - BudgetDao: Budget queries
  - GoalDao: Goal queries
  - TransactionGroupDao: Transaction group queries
- **Entities.kt**: Room entity definitions
  - TransactionEntity: Financial transactions
  - CategoryEntity: Transaction categories
  - PayeeEntity: Transaction payees
  - QuickTransactionEntity: Quick transaction templates
  - BudgetEntity: Budget tracking
  - GoalEntity: Savings goals
  - TransactionGroupEntity: Transaction groups

#### Network (`data/network/`)
- **dto/SupabaseDto.kt**: Supabase data transfer objects
  - SupabaseTransaction: Transaction DTO
  - SupabaseCategory: Category DTO
  - SupabasePayee: Payee DTO
  - SupabaseBudget: Budget DTO
  - SupabaseGoal: Goal DTO
  - SupabaseQuickTransaction: Quick transaction DTO
  - SupabaseGroup: Transaction group DTO

#### Repository (`data/repository/`)
- **AuthRepository.kt**: Authentication operations interface
  - signIn(): Sign in with email/password
  - signUp(): Sign up new user
  - signOut(): Sign out current user
  - getCurrentUserId(): Get current user ID
  - getCurrentSession(): Get current session
  - refreshSession(): Refresh access token
- **AuthRepositoryImpl.kt**: Supabase authentication implementation
- **SyncRepository.kt**: Synchronization operations interface
  - runFullSync(): Complete synchronization
  - runPartialSync(): Incremental synchronization
  - resetLocalData(): Clear local data
  - hasSyncedBefore(): Check sync history
  - getLastSyncTimestamp(): Get last sync time
- **SyncRepositoryImpl.kt**: Supabase sync implementation (593 lines)

#### Storage (`data/storage/`)
- **SecureStorage.kt**: Encrypted storage for session tokens
  - Uses AndroidX Security Crypto
  - Stores Supabase session
  - Provides secure get/set operations

### Dependency Injection (`di/`)

#### DatabaseModule
- Provides AppDatabase instance
- Provides all DAO instances
- Singleton scope

#### NetworkModule
- Provides SupabaseClient instance
- Provides Ktor OkHttp client
- Singleton scope

#### RepositoryModule
- Binds AuthRepository interface to AuthRepositoryImpl
- Binds SyncRepository interface to SyncRepositoryImpl
- Singleton scope

### Presentation Layer (`presentation/`)

#### MainActivity
- **MainActivity.kt**: Single activity host
  - Sets up Compose content
  - Handles navigation
  - Manages authentication flow

#### Auth Feature (`presentation/auth/`)
- **LoginScreen.kt**: Login UI composable
  - Email/password input
  - Sign in button
  - Loading state
  - Error display
- **LoginViewModel.kt**: Login logic
  - Validates credentials
  - Calls AuthRepository
  - Manages UI state

#### Budgets Feature (`presentation/budgets/`)
- **BudgetsScreen.kt**: Budgets UI composable
  - Budget cards list
  - Spending indicators
  - Swipe navigation
- **BudgetsViewModel.kt**: Budgets logic
  - Calculates spending
  - Computes daily advice
  - Manages sync state

#### Dashboard Feature (`presentation/dashboard/`)
- **DashboardScreen.kt**: Dashboard UI composable (966 lines)
  - Financial summary cards
  - Quick action buttons
  - Recent transactions
  - Transaction form overlays
  - Quick transactions overlay
- **DashboardViewModel.kt**: Dashboard logic
  - Calculates financial metrics
  - Manages transaction addition
  - Combines data flows

#### Goals Feature (`presentation/goals/`)
- **GoalsScreen.kt**: Goals UI composable
  - Goal cards list
  - Progress indicators
  - Swipe navigation
- **GoalsViewModel.kt**: Goals logic
  - Calculates progress
  - Computes remaining amount
  - Manages sync state

#### Settings Feature (`presentation/settings/`)
- **SettingsScreen.kt**: Settings UI composable
  - Theme selection
  - Sync controls
  - Logout option
  - Swipe navigation
- **SettingsViewModel.kt**: Settings logic
  - Manages theme preference
  - Handles sync reset
  - Manages logout

#### Sync Feature (`presentation/sync/`)
- **SyncScreen.kt**: Sync progress UI composable
  - Progress indicator
  - Status messages
  - Completion state
- **SyncViewModel.kt**: Sync progress logic
  - Triggers full sync
  - Reports progress
  - Handles completion

#### Theme (`presentation/theme/`)
- **Color.kt**: Color definitions
  - Primary colors
  - Secondary colors
  - Surface colors
  - Semantic colors (success, error, warning)
- **Theme.kt**: Material 3 theme configuration
  - Typography
  - Color scheme
  - Shapes

#### Utils (`presentation/utils/`)
- **Formatters.kt**: Formatting utilities
  - formatCurrency(): Currency formatting
  - formatRelativeTime(): Relative time formatting
  - formatTransactionDate(): Date formatting

## Proposed Modular Structure

For future scalability, the project could be restructured into feature-based modules:

```
app/                                    # Main application module
├── MainActivity.kt
└── di/
    └── AppModule.kt

core/                                   # Core module
├── theme/                              # Theme configuration
├── utils/                              # Shared utilities
└── design/                             # Design system

common/                                 # Common UI components
├── components/                         # Reusable composables
│   ├── cards/                          # Card components
│   ├── buttons/                        # Button components
│   └── loading/                        # Loading indicators
├── form/                               # Form components
│   ├── NumericKeypad.kt               # Custom keypad
│   └── Selectors.kt                   # Category/payee selectors
└── navigation/                         # Navigation utilities

data/                                   # Data module
├── database/                           # Room database
├── network/                            # Network layer
├── repository/                         # Repository implementations
└── storage/                            # Secure storage

feature-auth/                           # Authentication feature
├── presentation/
│   ├── LoginScreen.kt
│   └── LoginViewModel.kt
└── di/
    └── AuthModule.kt

feature-dashboard/                      # Dashboard feature
├── presentation/
│   ├── DashboardScreen.kt
│   ├── DashboardViewModel.kt
│   └── components/                     # Dashboard-specific components
└── di/
    └── DashboardModule.kt

feature-budgets/                         # Budgets feature
├── presentation/
│   ├── BudgetsScreen.kt
│   ├── BudgetsViewModel.kt
│   └── components/
└── di/
    └── BudgetsModule.kt

feature-goals/                          # Goals feature
├── presentation/
│   ├── GoalsScreen.kt
│   ├── GoalsViewModel.kt
│   └── components/
└── di/
    └── GoalsModule.kt

feature-settings/                       # Settings feature
├── presentation/
│   ├── SettingsScreen.kt
│   ├── SettingsViewModel.kt
│   └── components/
└── di/
    └── SettingsModule.kt

feature-sync/                           # Sync feature
├── presentation/
│   ├── SyncScreen.kt
│   └── SyncViewModel.kt
└── di/
    └── SyncModule.kt

feature-quickadd/                       # Quick add feature
├── presentation/
│   ├── TransactionForm.kt
│   ├── QuickTransactionsOverlay.kt
│   └── QuickAddViewModel.kt
└── di/
    └── QuickAddModule.kt

feature-tiles/                          # Tiles feature (Phase 9)
├── tiles/
│   ├── RemainingForPeriodTile.kt
│   ├── UpcomingPayDayTile.kt
│   ├── QuickAddExpenseTile.kt
│   ├── QuickAddIncomeTile.kt
│   ├── QuickTransactionTile.kt
│   ├── MonthlyExpenseTile.kt
│   ├── MonthlyIncomeTile.kt
│   ├── NetWorthTile.kt
│   ├── BudgetTile.kt
│   └── GoalTile.kt
└── di/
    └── TilesModule.kt

feature-complications/                   # Complications feature (Phase 10)
├── complications/
│   ├── PayDayCountdownComplication.kt
│   ├── QuickAddExpenseComplication.kt
│   ├── QuickAddIncomeComplication.kt
│   ├── QuickTransactionComplication.kt
│   ├── RemainingBalanceComplication.kt
│   └── NetWorthComplication.kt
└── di/
    └── ComplicationsModule.kt
```

## Module Dependencies

### Dependency Rules
- **app** depends on: all feature modules, core, common, data
- **feature-* modules** depend on: core, common, data
- **common** depends on: core
- **data** depends on: core
- **core** has no dependencies

### Dependency Graph
```
┌─────────────────────────────────────────┐
│                  app                    │
└──────┬────────────┬───────────────────┘
       │            │
       ▼            ▼
┌──────────────┐  ┌──────────────┐
│ feature-*    │  │ feature-*    │
│ (multiple)   │  │ (multiple)   │
└──────┬───────┘  └──────┬───────┘
       │                 │
       └────────┬────────┘
                │
       ┌────────▼────────┐
       │     common      │
       └────────┬────────┘
                │
       ┌────────▼────────┐
       │      core       │
       └─────────────────┘

       ┌─────────────────┐
       │      data       │
       └────────┬────────┘
                │
       ┌────────▼────────┐
       │      core       │
       └─────────────────┘
```

## Inter-Module Communication

### Data Access
- Feature modules access data through repository interfaces
- Data module provides repository implementations
- No direct database access from feature modules

### UI Components
- Common module provides reusable UI components
- Feature modules use common components
- Feature-specific components remain in feature modules

### Navigation
- App module manages navigation
- Feature modules expose navigation routes
- Common module provides navigation utilities

### Dependency Injection
- Each feature module has its own DI module
- App module aggregates all DI modules
- Core and data modules provide shared dependencies

## Benefits of Modular Structure

### Build Performance
- **Incremental Builds**: Only changed modules rebuild
- **Parallel Compilation**: Modules compile independently
- **Faster CI/CD**: Test modules in parallel

### Code Organization
- **Feature Boundaries**: Clear separation of concerns
- **Reduced Coupling**: Modules depend on interfaces
- **Easier Navigation**: Smaller, focused codebases

### Team Collaboration
- **Parallel Development**: Teams work on different modules
- **Reduced Conflicts**: Fewer merge conflicts
- **Clear Ownership**: Module ownership is clear

### Testing
- **Isolated Testing**: Test modules independently
- **Mock Dependencies**: Easy to mock module dependencies
- **Faster Tests**: Smaller test suites per module

### Scalability
- **Easy Addition**: Add new features as modules
- **Feature Flags**: Enable/disable modules at build time
- **Code Reuse**: Share modules across projects

## Migration Strategy

### Phase 1: Extract Core
1. Create `core` module
2. Move theme and utils to core
3. Update app to depend on core

### Phase 2: Extract Data
1. Create `data` module
2. Move database, network, repository to data
3. Update feature modules to depend on data

### Phase 3: Extract Common
1. Create `common` module
2. Extract reusable UI components
3. Update feature modules to use common

### Phase 4: Extract Features
1. Create feature modules one by one
2. Move feature code to respective modules
3. Update app to aggregate feature modules

### Phase 5: Add New Features
1. Create feature modules for tiles and complications
2. Implement features in isolated modules
3. Integrate with app module

## Current vs Proposed Comparison

### Current Structure
- **Single Module**: All code in app module
- **Package-Based**: Organization by package only
- **Tight Coupling**: Direct dependencies between packages
- **Monolithic Builds**: Full rebuild on any change

### Proposed Structure
- **Multi-Module**: Separated into logical modules
- **Feature-Based**: Organization by feature
- **Module Boundaries**: Clear interfaces between modules
- **Incremental Builds**: Only changed modules rebuild

## Module Configuration

### build.gradle.kts Structure
Each module would have its own `build.gradle.kts`:

```kotlin
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
}

dependencies {
    // Module-specific dependencies
    implementation(project(":core"))
    implementation(project(":common"))
    implementation(project(":data"))
    
    // External dependencies
    implementation(libs.compose.material3)
    implementation(libs.hilt.android)
    // ...
}
```

### Module Manifests
Feature modules would have minimal AndroidManifest.xml:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Feature-specific permissions, if any -->
</manifest>
```

## Conclusion

The current package structure is suitable for the current scope (Phase 1-8). As the project grows with Tiles (Phase 9) and Complications (Phase 10), migrating to a modular structure will provide better organization, faster builds, and improved maintainability. The proposed modular structure follows Android best practices and scales well for larger applications.
