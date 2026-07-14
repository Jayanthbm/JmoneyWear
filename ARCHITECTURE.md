# Jmoney Wear OS - Architecture Documentation

## Architecture Overview

Jmoney Wear OS follows a **Clean Architecture** pattern with **MVVM** (Model-View-ViewModel) presentation layer, **Repository Pattern** for data access, and **Dependency Injection** using Hilt. The architecture is designed to be testable, maintainable, and scalable.

## Architectural Layers

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                     │
│  (Composables, ViewModels, MainActivity)                 │
└────────────────────┬────────────────────────────────────┘
                     │ observes
┌────────────────────▼────────────────────────────────────┐
│                     Domain Layer                         │
│              (Repository Interfaces)                     │
└────────────────────┬────────────────────────────────────┘
                     │ implements
┌────────────────────▼────────────────────────────────────┐
│                      Data Layer                          │
│  (Repository Implementations, DAOs, DTOs, Storage)      │
└─────────────────────────────────────────────────────────┘
```

## Layer Responsibilities

### Presentation Layer
- **Location**: `presentation/` package
- **Responsibilities**:
  - UI rendering with Jetpack Compose
  - User interaction handling
  - State management with ViewModels
  - Navigation between screens
- **Key Components**:
  - `MainActivity.kt`: Single activity host
  - Screen composables (DashboardScreen, BudgetsScreen, etc.)
  - ViewModels (DashboardViewModel, BudgetsViewModel, etc.)
  - Theme configuration (Color.kt, Theme.kt)
  - Utility formatters (Formatters.kt)

### Domain Layer
- **Location**: `data/repository/` interfaces
- **Responsibilities**:
  - Define contracts for data operations
  - Abstract data sources from presentation
  - Enable testability through interfaces
- **Key Components**:
  - `AuthRepository.kt`: Authentication operations interface
  - `SyncRepository.kt`: Synchronization operations interface

### Data Layer
- **Location**: `data/` package
- **Responsibilities**:
  - Implement repository contracts
  - Manage local data storage (Room)
  - Handle network operations (Supabase)
  - Provide secure storage (EncryptedSharedPreferences)
- **Key Components**:
  - `database/`: Room database, entities, DAOs
  - `network/dto/`: Supabase data transfer objects
  - `repository/`: Repository implementations
  - `storage/`: Secure storage implementation

## Dependency Injection

Hilt is used for dependency injection, configured in the `di/` package:

### DatabaseModule
- Provides Room database instance
- Provides DAO instances
- Singleton scope

### NetworkModule
- Provides Supabase client
- Provides Ktor HTTP client
- Singleton scope

### RepositoryModule
- Binds repository interfaces to implementations
- Singleton scope

### Injection Points
- **ViewModels**: `@HiltViewModel` with `@Inject constructor`
- **Repositories**: `@Inject constructor` in implementations
- **Activities**: `@AndroidEntryPoint` annotation

## Data Flow Patterns

### Reactive Data Flow with Flow
```
DAO Flow → ViewModel (combine/transform) → StateFlow → UI (collectAsState)
```

**Example - Dashboard**:
1. `TransactionDao.getMonthlyIncomeFlow()` emits income changes
2. `DashboardViewModel` combines multiple DAO flows
3. Combined flow emits `DashboardState`
4. `DashboardScreen` collects state and recomposes

### Command Pattern for Actions
```
UI → ViewModel function → Repository → DAO/Network → Result
```

**Example - Add Transaction**:
1. User taps "Add Expense" button
2. `DashboardViewModel.addTransaction()` called
3. `TransactionDao.insertOrReplace()` inserts to Room
4. `SyncRepository.runPartialSync()` pushes to Supabase
5. Flow emits new state, UI updates

## Synchronization Architecture

### Sync Strategy
- **Push-Then-Pull**: Local changes pushed before pulling remote
- **Incremental Sync**: Transactions use tid-based incremental updates
- **Full Replace**: Reference data (categories, payees) fully replaced
- **Chunked Fetching**: Transactions fetched in 1000-record chunks

### Sync State Machine
```
Idle → Syncing(message) → Success
  ↓         ↓
Error ←─────┘
```

### Sync Flow
```
1. Check sync status (hasSyncedBefore)
2. Push local changes (all entities)
3. Pull transactions (chunked, tid-based)
4. Pull reference data (full replace)
5. Update sync timestamp
6. Emit SyncState.Success
```

## Database Architecture

### Room Database
- **Singleton**: Single database instance per app
- **Entities**: 7 tables matching Supabase schema
- **DAOs**: Type-safe query interfaces
- **Transactions**: Room transactions for atomic operations

### Denormalization Strategy
- **TransactionEntity**: Stores denormalized category/payee data
- **Reason**: Avoid joins for display queries on Wear OS
- **Trade-off**: Data redundancy for performance

### Sync Status Tracking
- **syncStatus field**: 0 = synced, 1 = unsynced
- **deleted field**: 0 = active, 1 = deleted
- **tid field**: Sequential transaction ID for incremental sync

## Authentication Architecture

### Session Management
- **Storage**: EncryptedSharedPreferences (AndroidX Security Crypto)
- **Persistence**: Session stored after successful login
- **Refresh**: Automatic token refresh on app launch
- **Fallback**: Manual login if session invalid

### Development Mode
- **Credentials**: Loaded from `local.properties`
- **BuildConfig**: Exposed as `DEV_EMAIL` and `DEV_PASSWORD`
- **Auto Sign-in**: Automatic sign-in if credentials present
- **Production**: Manual login screen for production builds

## UI Architecture

### Compose for Wear OS
- **Declarative UI**: UI described as functions of state
- **Recomposition**: Automatic UI updates on state changes
- **Material 3**: Design system for Wear OS

### Navigation Pattern
- **HorizontalPager**: Swipe navigation between main screens
- **Screen Order**: Dashboard ⇄ Budgets ⇄ Goals ⇄ Settings
- **Settings Access**: Also accessible via button on Dashboard

### Overlay Pattern
- **Transaction Form**: Full-screen overlay over Dashboard
- **Quick Transactions**: Full-screen overlay with preset list
- **Category/Payee Selection**: Sub-overlays within transaction form
- **Back Handlers**: Priority-based back handling for overlays

### State Management
- **ViewModel State**: `StateFlow<T>` for screen state
- **UI State**: `collectAsState()` for reactive updates
- **Local State**: `remember { mutableStateOf() }` for UI-only state

## Error Handling

### Repository Layer
- **Result Type**: Functions return `Result<T>`
- **Exception Handling**: `runCatching` blocks
- **Error Propagation**: Errors flow to ViewModel

### ViewModel Layer
- **Error States**: Included in state objects
- **User Feedback**: Toast messages for user-facing errors
- **Sync Errors**: Emitted in SyncState.Error

### UI Layer
- **Loading States**: CircularProgressIndicator during operations
- **Error Messages**: Toast or inline error display
- **Retry Mechanisms**: Manual sync button for retry

## Performance Considerations

### Database
- **Indexes**: Primary keys on all entities
- **Flow Queries**: Efficient reactive queries
- **Transactions**: Batch operations in transactions
- **Denormalization**: Avoid joins for display queries

### UI
- **Lazy Loading**: TransformingLazyColumn for efficient scrolling
- **State Persistence**: ViewModel survives configuration changes
- **Recomposition Scope**: Minimal recomposition with stable types
- **Large Touch Targets**: Wear OS optimized UI elements

### Network
- **Chunked Fetching**: 1000 records per chunk for transactions
- **Incremental Sync**: Only fetch new/changed transactions
- **Background Sync**: WorkManager integration (planned)

## Security Architecture

### Data at Rest
- **Encrypted Storage**: Session tokens encrypted
- **Database**: Room database (device encryption handled by OS)
- **SharedPreferences**: Standard (non-sensitive data only)

### Data in Transit
- **HTTPS**: All Supabase communication over HTTPS
- **Supabase SDK**: Built-in authentication and encryption
- **Token Management**: Access tokens refreshed automatically

### Credentials
- **Development Credentials**: In local.properties (not committed)
- **BuildConfig**: Compiled into APK (development builds only)
- **Production**: No hardcoded credentials

## Testing Strategy (Planned)

### Unit Tests
- **ViewModels**: Test state transformations
- **Repositories**: Test business logic
- **Formatters**: Test formatting functions
- **Calculations**: Test budget/goal calculations

### Integration Tests
- **DAO Tests**: Test database operations
- **Repository Tests**: Test sync logic with fake Supabase
- **Auth Tests**: Test authentication flow

### UI Tests
- **Compose Tests**: Test composable rendering
- **Navigation Tests**: Test screen transitions
- **User Flows**: Test complete user journeys

## Scalability Considerations

### Feature Modules
- **Current**: Monolithic app module
- **Planned**: Feature-based modules (dashboard, budgets, goals, etc.)
- **Benefit**: Faster builds, better separation of concerns

### Shared Components
- **Current**: Duplicated UI code across screens
- **Planned**: Common UI component library
- **Benefit**: Consistency, reusability

### Background Processing
- **Current**: Manual sync only
- **Planned**: WorkManager for periodic sync
- **Benefit**: Always up-to-date data, better UX

## Architecture Decision Records

### ADR-001: Use Room for Local Storage
**Status**: Accepted
**Context**: Need offline-first experience on Wear OS
**Decision**: Use Room database with Flow queries
**Consequences**: 
- Pros: Type-safe, reactive, offline support
- Cons: Additional complexity, schema management

### ADR-002: Denormalize Transaction Data
**Status**: Accepted
**Context**: Wear OS has limited resources, joins are expensive
**Decision**: Store category/payee data in TransactionEntity
**Consequences**:
- Pros: Faster display queries, no joins
- Cons: Data redundancy, update complexity

### ADR-003: Use RemoteInput for Text Entry
**Status**: Accepted (Temporary)
**Context**: Need numeric input for transaction amounts
**Decision**: Use Wear OS RemoteInput with system keyboard
**Consequences**:
- Pros: System integration, familiar UX
- Cons: Not optimized for watch, planned custom keypad

### ADR-004: Push-Then-Pull Sync Strategy
**Status**: Accepted
**Context**: Need bidirectional sync with Supabase
**Decision**: Push local changes before pulling remote
**Consequences**:
- Pros: Conflict prevention, data consistency
- Cons: Slower sync, more network calls

### ADR-005: Tid-based Incremental Sync
**Status**: Accepted
**Context**: Need efficient sync for large transaction history
**Decision**: Use sequential tid for incremental updates
**Consequences**:
- Pros: Fast sync, minimal data transfer
- Cons: Requires tid management, not supported by all entities

## Future Architectural Improvements

### Short Term
1. Extract reusable UI components
2. Split large ViewModels
3. Implement custom numeric keypad
4. Add WorkManager for background sync

### Medium Term
1. Modularize into feature modules
2. Add comprehensive test coverage
3. Implement offline queue for failed syncs
4. Add analytics and crash reporting

### Long Term
1. Consider multi-module architecture
2. Implement data caching strategy
3. Add predictive pre-fetching
4. Explore Kotlin Multiplatform for shared logic
