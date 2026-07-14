# Jmoney Wear OS - Phase 9 Implementation Plan: Tiles

## Overview

Phase 9 implements 10 Wear OS tiles to provide quick access to financial information and actions from the watch face. Tiles are glanceable, full-screen experiences that users can add to their watch face for instant access to key features.

## Tile Specifications

### 1. Remaining For Period Tile

**Purpose**: Display remaining budget or overspent amount for current period

**Data Required**:
- Monthly income
- Monthly expense
- Remaining calculation (income - expense)
- Overspent status

**Layout**:
- Large currency value (remaining or overspent)
- Label: "REMAINING" or "OVERSPENT"
- Color: Green for remaining, Red for overspent
- Last updated timestamp

**Update Frequency**: Every 15 minutes

**Battery Optimization**:
- Use lazy loading
- Cache data in memory
- Only recalculate when data changes

**Implementation Order**: 1 (first tile)

---

### 2. Upcoming Pay Day Tile

**Purpose**: Display countdown to next pay day

**Data Required**:
- Days remaining until next month
- Next pay date (assumed 1st of next month)

**Layout**:
- Large number: "X days"
- Label: "TO PAY DAY"
- Next pay date: "MMM DD"
- Calendar icon

**Update Frequency**: Every 30 minutes

**Battery Optimization**:
- Static calculation (no database query)
- Minimal battery impact

**Implementation Order**: 2

---

### 3. Quick Add Expense Tile

**Purpose**: Quick access to add expense transaction

**Data Required**:
- Categories list (expense type)
- Default expense category

**Layout**:
- Large minus icon (➖)
- Label: "ADD EXPENSE"
- Tap to open expense form

**Update Frequency**: On tap only

**Battery Optimization**:
- No background updates
- Only launches activity on tap

**Implementation Order**: 3

---

### 4. Quick Add Income Tile

**Purpose**: Quick access to add income transaction

**Data Required**:
- Categories list (income type)
- Default income category

**Layout**:
- Large plus icon (➕)
- Label: "ADD INCOME"
- Tap to open income form

**Update Frequency**: On tap only

**Battery Optimization**:
- No background updates
- Only launches activity on tap

**Implementation Order**: 4

---

### 5. Quick Transaction Tile

**Purpose**: Quick access to quick transaction presets

**Data Required**:
- Quick transactions list
- Priority ordering

**Layout**:
- Lightning bolt icon (⚡)
- Label: "QUICK ADD"
- Tap to open quick transactions overlay

**Update Frequency**: On tap only

**Battery Optimization**:
- No background updates
- Only launches activity on tap

**Implementation Order**: 5

---

### 6. Monthly Expense Tile

**Purpose**: Display total monthly expenses

**Data Required**:
- Monthly expense total
- Current month

**Layout**:
- Large currency value
- Label: "MONTHLY EXPENSE"
- Month indicator: "JAN", "FEB", etc.
- Trend indicator (if available)

**Update Frequency**: Every 30 minutes

**Battery Optimization**:
- Cache monthly expense
- Only recalculate when transactions change

**Implementation Order**: 6

---

### 7. Monthly Income Tile

**Purpose**: Display total monthly income

**Data Required**:
- Monthly income total
- Current month

**Layout**:
- Large currency value
- Label: "MONTHLY INCOME"
- Month indicator: "JAN", "FEB", etc.
- Trend indicator (if available)

**Update Frequency**: Every 30 minutes

**Battery Optimization**:
- Cache monthly income
- Only recalculate when transactions change

**Implementation Order**: 7

---

### 8. Net Worth Tile

**Purpose**: Display current net worth

**Data Required**:
- Net worth calculation (total income - total expense)

**Layout**:
- Large currency value
- Label: "NET WORTH"
- Color: Primary brand color
- Last updated timestamp

**Update Frequency**: Every 30 minutes

**Battery Optimization**:
- Cache net worth
- Only recalculate when transactions change

**Implementation Order**: 8

---

### 9. Budget Tile (Configurable)

**Purpose**: Display spending for a specific budget

**Data Required**:
- Budget list
- User-selected budget
- Budget spending calculation
- Budget progress percentage

**Configuration**:
- User selects budget when adding tile
- Tile shows selected budget's spending
- Configuration UI for budget selection

**Layout**:
- Budget name (truncated if needed)
- Spent / Total amount
- Progress bar
- Percentage used
- Daily spend advice

**Update Frequency**: Every 15 minutes

**Battery Optimization**:
- Cache budget data
- Only recalculate when transactions change
- Lazy loading for budget list

**Implementation Order**: 9

---

### 10. Goal Tile (Configurable)

**Purpose**: Display progress for a specific goal

**Data Required**:
- Goal list
- User-selected goal
- Goal progress calculation
- Goal completion status

**Configuration**:
- User selects goal when adding tile
- Tile shows selected goal's progress
- Configuration UI for goal selection

**Layout**:
- Goal name (truncated if needed)
- Saved / Goal amount
- Progress bar
- Percentage completed
- Remaining amount

**Update Frequency**: Every 15 minutes

**Battery Optimization**:
- Cache goal data
- Only recalculate when goals change
- Lazy loading for goal list

**Implementation Order**: 10

---

## Shared Architecture

### Tile Service Structure

```
presentation/tiles/
├── JmoneyTileService.kt                 # Main tile service
├── tiles/
│   ├── RemainingForPeriodTile.kt       # Tile 1
│   ├── UpcomingPayDayTile.kt           # Tile 2
│   ├── QuickAddExpenseTile.kt          # Tile 3
│   ├── QuickAddIncomeTile.kt          # Tile 4
│   ├── QuickTransactionTile.kt        # Tile 5
│   ├── MonthlyExpenseTile.kt          # Tile 6
│   ├── MonthlyIncomeTile.kt           # Tile 7
│   ├── NetWorthTile.kt                # Tile 8
│   ├── BudgetTile.kt                  # Tile 9
│   └── GoalTile.kt                    # Tile 10
├── configuration/
│   ├── BudgetTileConfigActivity.kt    # Budget selection UI
│   └── GoalTileConfigActivity.kt       # Goal selection UI
└── utils/
    ├── TileDataFetcher.kt             # Data fetching utilities
    └── TileResourceBuilder.kt         # Resource building utilities
```

### Tile Service Base

**JmoneyTileService.kt**:
- Extends `WearTileService`
- Registers all tile IDs
- Delegates to specific tile implementations
- Handles tile lifecycle

### Tile Implementation Pattern

Each tile follows this pattern:
```kotlin
class SpecificTile : TileProvider {
    override fun tileBuilder(
        context: Context,
        deviceParameters: DeviceParameters,
        tileState: TileState
    ): TileBuilder {
        // Fetch data
        val data = fetchData(context)
        
        // Build layout
        return LayoutElementBuilders.Layout.Builder()
            .setRoot(buildLayout(data))
            .build()
            .toTileBuilder()
            .setResourcesVersion(RESOURCES_VERSION)
    }
    
    override fun onTileEnterEvent(
        context: Context,
        tileId: String,
        tileState: TileState
    ) {
        // Handle tile enter (if needed)
    }
    
    override fun onTileLeaveEvent(
        context: Context,
        tileId: String,
        tileState: TileState
    ) {
        // Handle tile leave (if needed)
    }
    
    private fun fetchData(context: Context): TileData {
        // Fetch data from database
        // Use coroutine for async operations
    }
    
    private fun buildLayout(data: TileData): LayoutElement {
        // Build tile layout using LayoutElementBuilders
    }
}
```

### Data Fetching Strategy

**TileDataFetcher.kt**:
- Provides coroutine-based data fetching
- Caches data to reduce database queries
- Handles authentication state
- Provides fallback data when not authenticated

```kotlin
class TileDataFetcher @Inject constructor(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val goalDao: GoalDao,
    private val authRepository: AuthRepository
) {
    suspend fun fetchRemainingForPeriod(): Double {
        // Fetch monthly income and expense
        // Calculate remaining
    }
    
    suspend fun fetchMonthlyExpense(): Double {
        // Fetch monthly expense
    }
    
    suspend fun fetchMonthlyIncome(): Double {
        // Fetch monthly income
    }
    
    suspend fun fetchNetWorth(): Double {
        // Fetch net worth
    }
    
    suspend fun fetchBudgetSpending(budgetId: String): BudgetSpending {
        // Fetch budget spending
    }
    
    suspend fun fetchGoalProgress(goalId: String): GoalProgress {
        // Fetch goal progress
    }
}
```

### Resource Building Strategy

**TileResourceBuilder.kt**:
- Provides resource building utilities
- Manages resource versioning
- Handles image resources
- Provides consistent styling

```kotlin
class TileResourceBuilder {
    companion object {
        const val RESOURCES_VERSION = "1"
        
        fun onTileResourcesRequest(
            context: Context,
            deviceParameters: DeviceParameters,
            resourceIds: List<String>
        ): Resources.Builder {
            return Resources.Builder()
                .addIdToImageMapping(...)
                .build()
        }
    }
}
```

## Implementation Order

### Phase 9.1: Infrastructure (1-2 days)
1. Create tile service structure
2. Implement TileDataFetcher
3. Implement TileResourceBuilder
4. Set up AndroidManifest.xml for tiles
5. Test infrastructure

### Phase 9.2: Information Tiles (2-3 days)
1. Implement Remaining For Period Tile
2. Implement Upcoming Pay Day Tile
3. Implement Monthly Expense Tile
4. Implement Monthly Income Tile
5. Implement Net Worth Tile
6. Test all information tiles

### Phase 9.3: Action Tiles (1-2 days)
1. Implement Quick Add Expense Tile
2. Implement Quick Add Income Tile
3. Implement Quick Transaction Tile
4. Set up activity launching from tiles
5. Test all action tiles

### Phase 9.4: Configurable Tiles (2-3 days)
1. Implement Budget Tile
2. Implement Budget Tile Config Activity
3. Implement Goal Tile
4. Implement Goal Tile Config Activity
5. Test configurable tiles

### Phase 9.5: Polish & Testing (1-2 days)
1. Add animations
2. Optimize battery usage
3. Test on different screen sizes
4. Test offline behavior
5. Final testing

## Data Flow

### Information Tiles Data Flow
```
Tile Service → TileDataFetcher → Room Database → Tile Layout → Display
```

### Action Tiles Data Flow
```
Tile Tap → MainActivity → Specific Screen → Transaction Form
```

### Configurable Tiles Data Flow
```
Tile Add → Config Activity → User Selection → Save Preference → Tile Display
Tile Update → Read Preference → Fetch Data → Tile Layout → Display
```

## Battery Optimization Strategies

### 1. Lazy Loading
- Only fetch data when tile is visible
- Use coroutine for async operations
- Cancel coroutines when tile is not visible

### 2. Caching
- Cache data in memory
- Use SharedPreferences for configuration
- Only recalculate when data changes

### 3. Update Frequency
- Information tiles: 15-30 minutes
- Action tiles: On tap only
- Configurable tiles: 15 minutes

### 4. Efficient Queries
- Use indexed database queries
- Limit query results
- Use Flow for reactive updates

### 5. Resource Management
- Use lightweight resources
- Avoid heavy operations in tile service
- Use WorkManager for background updates

## Testing Strategy

### Unit Tests
- Test TileDataFetcher methods
- Test data calculations
- Test resource building

### Integration Tests
- Test tile service integration
- Test database queries
- Test configuration persistence

### UI Tests
- Test tile display on emulator
- Test tile configuration UI
- Test activity launching from tiles

### Manual Tests
- Test on physical device
- Test battery impact
- Test offline behavior
- Test with different screen sizes

## AndroidManifest.xml Configuration

Add tile service declarations:

```xml
<service
    android:name=".presentation.tiles.JmoneyTileService"
    android:label="@string/tile_service_label"
    android:description="@string/tile_service_description"
    android:icon="@drawable/ic_tile_icon"
    android:exported="true">
    <intent-filter>
        <action android:name="androidx.wear.tiles.action.BIND_TILE_PROVIDER" />
    </intent-filter>
    
    <meta-data
        android:name="androidx.wear.tiles.PREVIEW"
        android:resource="@xml/tile_preview" />
</service>
```

Add tile configuration activities:

```xml
<activity
    android:name=".presentation.tiles.configuration.BudgetTileConfigActivity"
    android:label="@string/budget_tile_config_label"
    android:exported="true">
    <intent-filter>
        <action android:name="androidx.wear.tiles.action.CONFIGURE_TILE" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</activity>

<activity
    android:name=".presentation.tiles.configuration.GoalTileConfigActivity"
    android:label="@string/goal_tile_config_label"
    android:exported="true">
    <intent-filter>
        <action android:name="androidx.wear.tiles.action.CONFIGURE_TILE" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</activity>
```

## Dependencies

Add to `app/build.gradle.kts`:

```kotlin
// Wear Tiles
implementation(libs.wear.tiles)
implementation(libs.wear.tiles.tooling)
implementation(libs.wear.tiles.tooling.preview)

// Horologist (optional, for better tile support)
implementation(libs.horologist.tiles)
implementation(libs.horologist.compose.layout)
```

## Known Challenges

### 1. Tile Layout Complexity
- **Challenge**: Wear OS tile layout API is verbose
- **Solution**: Use helper functions and layout builders
- **Alternative**: Consider Horologist library for easier layout building

### 2. Data Fetching in Tile Service
- **Challenge**: Tile service runs in restricted context
- **Solution**: Use coroutines and dependency injection
- **Fallback**: Provide cached data when fetch fails

### 3. Configuration UI
- **Challenge**: Limited screen space for configuration
- **Solution**: Use scrolling lists and simplified UI
- **Alternative**: Use phone companion for configuration

### 4. Battery Impact
- **Challenge**: Frequent updates drain battery
- **Solution**: Optimize update frequency and caching
- **Monitoring**: Use battery historian to monitor impact

### 5. Authentication State
- **Challenge**: Tiles need to handle unauthenticated state
- **Solution**: Show login prompt when not authenticated
- **Fallback**: Display cached data with "not updated" indicator

## Success Criteria

### Functional
- All 10 tiles implemented and working
- Tiles display correct data
- Action tiles launch correct screens
- Configurable tiles save and load configuration
- All tiles update at correct frequency

### Performance
- Tiles load within 1 second
- Battery impact < 2% per day
- No memory leaks
- Smooth animations

### User Experience
- Tiles are glanceable and readable
- Configuration UI is intuitive
- Tiles work offline
- Tiles handle authentication state gracefully

### Code Quality
- Clean, maintainable code
- Shared utilities where appropriate
- Comprehensive error handling
- No code duplication

## Rollback Plan

If issues arise during implementation:
1. Disable problematic tiles in manifest
2. Keep working tiles functional
3. Fix issues in separate branch
4. Test thoroughly before re-enabling

## Next Steps

After Phase 9 completion:
1. Test all tiles thoroughly
2. Gather user feedback
3. Optimize based on feedback
4. Proceed to Phase 10 (Complications)

## Conclusion

Phase 9 will implement 10 Wear OS tiles to provide quick access to financial information and actions. The implementation follows a phased approach, building infrastructure first, then implementing tiles by category (information, action, configurable). Battery optimization is a key consideration throughout. The tiles will enhance the user experience by providing glanceable information and quick actions directly from the watch face.
