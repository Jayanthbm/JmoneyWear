# Jmoney Wear OS - Phase 10 Implementation Plan: Complications

## Overview

Phase 10 implements 6 Wear OS complications to provide financial information directly on the watch face. Complications are small data providers that watch faces can display in various slots, offering at-a-glance information without leaving the watch face.

## Complication Specifications

### 1. Pay Day Countdown Complication

**Purpose**: Display countdown to next pay day on watch face

**Data Required**:
- Days remaining until next month
- Next pay date (assumed 1st of next month)

**Supported Types**:
- SHORT_TEXT: "X days"
- LONG_TEXT: "X days to pay"
- RANGED_VALUE: Progress bar to pay day
- SMALL_IMAGE: Calendar icon with day count

**Layout Variants**:
- Small: Icon + day count
- Medium: "X days" label
- Large: "X days to pay" with progress

**Update Frequency**: Every 30 minutes

**Battery Optimization**:
- Static calculation (no database query)
- Minimal battery impact
- No background sync needed

**Implementation Order**: 1 (first complication)

---

### 2. Quick Add Expense Complication

**Purpose**: Quick access to add expense from watch face

**Data Required**:
- Default expense category
- Categories list (for future enhancement)

**Supported Types**:
- ICON: Minus icon (➖)
- SHORT_TEXT: "Expense"
- LONG_TEXT: "Add Expense"

**Layout Variants**:
- Small: Minus icon
- Medium: "Expense" text
- Large: "Add Expense" with icon

**Update Frequency**: On tap only

**Battery Optimization**:
- No background updates
- Only launches activity on tap
- Zero battery impact when not tapped

**Implementation Order**: 2

---

### 3. Quick Add Income Complication

**Purpose**: Quick access to add income from watch face

**Data Required**:
- Default income category
- Categories list (for future enhancement)

**Supported Types**:
- ICON: Plus icon (➕)
- SHORT_TEXT: "Income"
- LONG_TEXT: "Add Income"

**Layout Variants**:
- Small: Plus icon
- Medium: "Income" text
- Large: "Add Income" with icon

**Update Frequency**: On tap only

**Battery Optimization**:
- No background updates
- Only launches activity on tap
- Zero battery impact when not tapped

**Implementation Order**: 3

---

### 4. Quick Transaction Complication

**Purpose**: Quick access to quick transaction presets from watch face

**Data Required**:
- Quick transactions count
- Priority quick transaction (for future enhancement)

**Supported Types**:
- ICON: Lightning bolt icon (⚡)
- SHORT_TEXT: "Quick"
- LONG_TEXT: "Quick Add"

**Layout Variants**:
- Small: Lightning icon
- Medium: "Quick" text
- Large: "Quick Add" with icon

**Update Frequency**: On tap only

**Battery Optimization**:
- No background updates
- Only launches activity on tap
- Zero battery impact when not tapped

**Implementation Order**: 4

---

### 5. Remaining Balance Complication

**Purpose**: Display remaining budget or overspent amount on watch face

**Data Required**:
- Monthly income
- Monthly expense
- Remaining calculation (income - expense)
- Overspent status

**Supported Types**:
- SHORT_TEXT: Currency value
- LONG_TEXT: "Remaining: ₹X" or "Overspent: ₹X"
- RANGED_VALUE: Progress bar (remaining / income)
- SMALL_IMAGE: Wallet icon with value

**Layout Variants**:
- Small: Icon + abbreviated value
- Medium: Currency value
- Large: "Remaining: ₹X" with color indicator

**Update Frequency**: Every 15 minutes

**Battery Optimization**:
- Cache data in memory
- Only recalculate when transactions change
- Use efficient database queries

**Implementation Order**: 5

---

### 6. Net Worth Complication

**Purpose**: Display current net worth on watch face

**Data Required**:
- Net worth calculation (total income - total expense)

**Supported Types**:
- SHORT_TEXT: Currency value
- LONG_TEXT: "Net Worth: ₹X"
- RANGED_VALUE: Progress bar (if target set)
- SMALL_IMAGE: Chart icon with value

**Layout Variants**:
- Small: Icon + abbreviated value
- Medium: Currency value
- Large: "Net Worth: ₹X" with trend indicator

**Update Frequency**: Every 30 minutes

**Battery Optimization**:
- Cache net worth in memory
- Only recalculate when transactions change
- Use efficient database queries

**Implementation Order**: 6

---

## Shared Architecture

### Complication Service Structure

```
presentation/complications/
├── JmoneyComplicationService.kt         # Main complication service
├── complications/
│   ├── PayDayCountdownComplication.kt  # Complication 1
│   ├── QuickAddExpenseComplication.kt  # Complication 2
│   ├── QuickAddIncomeComplication.kt   # Complication 3
│   ├── QuickTransactionComplication.kt # Complication 4
│   ├── RemainingBalanceComplication.kt # Complication 5
│   └── NetWorthComplication.kt         # Complication 6
└── utils/
    ├── ComplicationDataFetcher.kt      # Data fetching utilities
    └── ComplicationResourceBuilder.kt  # Resource building utilities
```

### Complication Service Base

**JmoneyComplicationService.kt**:
- Extends `ComplicationProviderService`
- Registers all complication IDs
- Delegates to specific complication implementations
- Handles complication lifecycle

### Complication Implementation Pattern

Each complication follows this pattern:
```kotlin
class SpecificComplication : ComplicationProvider {
    override fun onComplicationRequest(
        request: ComplicationRequest
    ): ComplicationData {
        // Determine supported types
        val supportedTypes = getSupportedTypes(request.complicationType)
        
        // Fetch data
        val data = fetchData(request)
        
        // Build complication data based on type
        return when (request.complicationType) {
            ComplicationType.SHORT_TEXT -> buildShortText(data)
            ComplicationType.LONG_TEXT -> buildLongText(data)
            ComplicationType.RANGED_VALUE -> buildRangedValue(data)
            ComplicationType.SMALL_IMAGE -> buildSmallImage(data)
            ComplicationType.ICON -> buildIcon(data)
            else -> buildNoData()
        }
    }
    
    override fun onComplicationActivated(
        complicationInstanceId: ComplicationInstanceId
    ) {
        // Handle activation (if needed)
    }
    
    override fun onComplicationDeactivated(
        complicationInstanceId: ComplicationInstanceId
    ) {
        // Handle deactivation (if needed)
    }
    
    private fun fetchData(request: ComplicationRequest): ComplicationData {
        // Fetch data from database
        // Use coroutine for async operations
    }
    
    private fun buildShortText(data: ComplicationData): ComplicationData {
        // Build short text complication
    }
    
    private fun buildLongText(data: ComplicationData): ComplicationData {
        // Build long text complication
    }
    
    private fun buildRangedValue(data: ComplicationData): ComplicationData {
        // Build ranged value complication
    }
    
    private fun buildSmallImage(data: ComplicationData): ComplicationData {
        // Build small image complication
    }
    
    private fun buildIcon(data: ComplicationData): ComplicationData {
        // Build icon complication
    }
}
```

### Data Fetching Strategy

**ComplicationDataFetcher.kt**:
- Provides coroutine-based data fetching
- Caches data to reduce database queries
- Handles authentication state
- Provides fallback data when not authenticated

```kotlin
class ComplicationDataFetcher @Inject constructor(
    private val transactionDao: TransactionDao,
    private val authRepository: AuthRepository
) {
    private val cache = ComplicationDataCache()
    
    suspend fun fetchPayDayCountdown(): Int {
        // Calculate days to next month
        // Static calculation, no database query
    }
    
    suspend fun fetchRemainingBalance(): Double {
        // Check cache first
        cache.getRemainingBalance()?.let { return it }
        
        // Fetch from database
        val income = transactionDao.getMonthlyIncome(...)
        val expense = transactionDao.getMonthlyExpense(...)
        val remaining = income - expense
        
        // Cache result
        cache.setRemainingBalance(remaining)
        return remaining
    }
    
    suspend fun fetchNetWorth(): Double {
        // Check cache first
        cache.getNetWorth()?.let { return it }
        
        // Fetch from database
        val netWorth = transactionDao.getNetWorth(...)
        
        // Cache result
        cache.setNetWorth(netWorth)
        return netWorth
    }
    
    fun clearCache() {
        cache.clear()
    }
}
```

### Resource Building Strategy

**ComplicationResourceBuilder.kt**:
- Provides resource building utilities
- Manages icon resources
- Provides consistent styling
- Handles different complication types

```kotlin
class ComplicationResourceBuilder {
    companion object {
        fun buildShortText(
            text: String,
            title: String? = null
        ): ComplicationData {
            return PlainComplicationText.Builder()
                .setText(text)
                .build()
                .let { text ->
                    if (title != null) {
                        ComplicationData.Builder(
                            ComplicationType.SHORT_TEXT
                        )
                            .setShortText(text)
                            .setTitle(PlainComplicationText.Builder().setText(title).build())
                            .build()
                    } else {
                        ComplicationData.Builder(
                            ComplicationType.SHORT_TEXT
                        )
                            .setShortText(text)
                            .build()
                    }
                }
        }
        
        fun buildLongText(
            text: String,
            title: String? = null
        ): ComplicationData {
            return PlainComplicationText.Builder()
                .setText(text)
                .build()
                .let { text ->
                    if (title != null) {
                        ComplicationData.Builder(
                            ComplicationType.LONG_TEXT
                        )
                            .setLongText(text)
                            .setTitle(PlainComplicationText.Builder().setText(title).build())
                            .build()
                    } else {
                        ComplicationData.Builder(
                            ComplicationType.LONG_TEXT
                        )
                            .setLongText(text)
                            .build()
                    }
                }
        }
        
        fun buildRangedValue(
            value: Float,
            min: Float,
            max: Float,
            text: String? = null
        ): ComplicationData {
            return ComplicationData.Builder(
                ComplicationType.RANGED_VALUE
            )
                .setRangedValue(
                    RangedValue.Builder()
                        .setValue(value)
                        .setMin(min)
                        .setMax(max)
                        .build()
                )
                .apply {
                    text?.let {
                        setText(PlainComplicationText.Builder().setText(it).build())
                    }
                }
                .build()
        }
        
        fun buildSmallImage(
            icon: Icon
        ): ComplicationData {
            return ComplicationData.Builder(
                ComplicationType.SMALL_IMAGE
            )
                .setSmallImage(icon)
                .build()
        }
        
        fun buildIcon(icon: Icon): ComplicationData {
            return ComplicationData.Builder(
                ComplicationType.ICON
            )
                .setIcon(icon)
                .build()
        }
    }
}
```

## Implementation Order

### Phase 10.1: Infrastructure (1-2 days)
1. Create complication service structure
2. Implement ComplicationDataFetcher
3. Implement ComplicationResourceBuilder
4. Set up AndroidManifest.xml for complications
5. Test infrastructure

### Phase 10.2: Action Complications (1-2 days)
1. Implement Quick Add Expense Complication
2. Implement Quick Add Income Complication
3. Implement Quick Transaction Complication
4. Set up activity launching from complications
5. Test all action complications

### Phase 10.3: Information Complications (2-3 days)
1. Implement Pay Day Countdown Complication
2. Implement Remaining Balance Complication
3. Implement Net Worth Complication
4. Test all information complications

### Phase 10.4: Polish & Testing (1-2 days)
1. Add icon resources
2. Optimize battery usage
3. Test on different watch faces
4. Test offline behavior
5. Final testing

## Data Flow

### Information Complications Data Flow
```
Complication Service → ComplicationDataFetcher → Room Database → Complication Data → Watch Face
```

### Action Complications Data Flow
```
Complication Tap → MainActivity → Specific Screen → Transaction Form
```

## Battery Optimization Strategies

### 1. Lazy Loading
- Only fetch data when complication is requested
- Use coroutine for async operations
- Cancel coroutines when complication is deactivated

### 2. Caching
- Cache data in memory with time-based expiration
- Use SharedPreferences for configuration
- Only recalculate when data changes

### 3. Update Frequency
- Information complications: 15-30 minutes
- Action complications: On tap only
- Use WorkManager for scheduled updates (if needed)

### 4. Efficient Queries
- Use indexed database queries
- Limit query results
- Use Flow for reactive updates

### 5. Resource Management
- Use lightweight icons
- Avoid heavy operations in complication service
- Use background threads for data fetching

## Testing Strategy

### Unit Tests
- Test ComplicationDataFetcher methods
- Test data calculations
- Test resource building
- Test cache expiration

### Integration Tests
- Test complication service integration
- Test database queries
- Test cache behavior

### UI Tests
- Test complication display on different watch faces
- Test activity launching from complications
- Test complication activation/deactivation

### Manual Tests
- Test on physical device
- Test battery impact
- Test offline behavior
- Test with different watch faces

## AndroidManifest.xml Configuration

Add complication service declarations:

```xml
<service
    android:name=".presentation.complications.JmoneyComplicationService"
    android:label="@string/complication_service_label"
    android:description="@string/complication_service_description"
    android:icon="@drawable/ic_complication_icon"
    android:exported="true">
    <intent-filter>
        <action android:name="android.support.wearable.complications.ACTION_COMPLICATION_UPDATE_REQUEST" />
    </intent-filter>
    
    <meta-data
        android:name="android.support.wearable.complications.SUPPORTED_TYPES"
        android:value="SHORT_TEXT,LONG_TEXT,RANGED_VALUE,SMALL_IMAGE,ICON" />
    
    <meta-data
        android:name="android.support.wearable.complications.UPDATE_PERIOD_SECONDS"
        android:value="1800" />
</service>
```

## Complication IDs

Each complication needs a unique ID:

```kotlin
object ComplicationIds {
    const val PAY_DAY_COUNTDOWN = "pay_day_countdown"
    const val QUICK_ADD_EXPENSE = "quick_add_expense"
    const val QUICK_ADD_INCOME = "quick_add_income"
    const val QUICK_TRANSACTION = "quick_transaction"
    const val REMAINING_BALANCE = "remaining_balance"
    const val NET_WORTH = "net_worth"
}
```

## Icon Resources

Create icon resources for complications:

```
res/drawable/
├── ic_complication_payday.xml
├── ic_complication_expense.xml
├── ic_complication_income.xml
├── ic_complication_quick.xml
├── ic_complication_balance.xml
└── ic_complication_networth.xml
```

## Dependencies

Add to `app/build.gradle.kts`:

```kotlin
// Wear Complications
implementation(libs.wear.complications)
implementation(libs.wear.complications.datasource)
```

## Known Challenges

### 1. Complication Type Support
- **Challenge**: Different watch faces support different complication types
- **Solution**: Support multiple types per complication
- **Fallback**: Provide default type if requested type not supported

### 2. Data Fetching in Complication Service
- **Challenge**: Complication service runs in restricted context
- **Solution**: Use coroutines and dependency injection
- **Fallback**: Provide cached data when fetch fails

### 3. Update Frequency
- **Challenge**: Frequent updates drain battery
- **Solution**: Optimize update frequency and caching
- **Monitoring**: Use battery historian to monitor impact

### 4. Watch Face Compatibility
- **Challenge**: Different watch faces have different layouts
- **Solution**: Support multiple complication types and sizes
- **Testing**: Test on popular watch faces

### 5. Authentication State
- **Challenge**: Complications need to handle unauthenticated state
- **Solution**: Show login prompt or cached data
- **Fallback**: Display "--" when not authenticated

## Comparison with Tiles

### Tiles vs Complications

| Aspect | Tiles | Complications |
|--------|-------|---------------|
| Display | Full screen | Small slots on watch face |
| Interaction | Tap to expand | Tap to launch action |
| Update Frequency | 15-30 minutes | 15-30 minutes |
| Battery Impact | Medium | Low |
| Customization | Configurable UI | Limited by watch face |
| Data Display | Rich data | Limited data |

### Shared Components

Both Tiles and Complications can share:
- ComplicationDataFetcher (can be renamed to TileComplicationDataFetcher)
- Resource building utilities
- Data caching logic
- Authentication state handling

## Success Criteria

### Functional
- All 6 complications implemented and working
- Complications display correct data
- Action complications launch correct screens
- All complications update at correct frequency
- Complications work on different watch faces

### Performance
- Complications load within 500ms
- Battery impact < 1% per day
- No memory leaks
- Smooth updates

### User Experience
- Complications are readable on watch face
- Action complications respond quickly
- Complications work offline
- Complications handle authentication state gracefully

### Code Quality
- Clean, maintainable code
- Shared utilities with tiles
- Comprehensive error handling
- No code duplication

## Rollback Plan

If issues arise during implementation:
1. Disable problematic complications in manifest
2. Keep working complications functional
3. Fix issues in separate branch
4. Test thoroughly before re-enabling

## Integration with Tiles

### Shared Data Fetcher
Create a unified data fetcher for both tiles and complications:

```kotlin
class TileComplicationDataFetcher @Inject constructor(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val goalDao: GoalDao,
    private val authRepository: AuthRepository
) {
    // Shared methods for both tiles and complications
    suspend fun fetchRemainingBalance(): Double { ... }
    suspend fun fetchNetWorth(): Double { ... }
    suspend fun fetchPayDayCountdown(): Int { ... }
    
    // Tile-specific methods
    suspend fun fetchBudgetSpending(budgetId: String): BudgetSpending { ... }
    suspend fun fetchGoalProgress(goalId: String): GoalProgress { ... }
}
```

### Shared Resource Builder
Create a unified resource builder for both tiles and complications:

```kotlin
class TileComplicationResourceBuilder {
    // Shared icon resources
    fun getPayDayIcon(): Icon { ... }
    fun getExpenseIcon(): Icon { ... }
    fun getIncomeIcon(): Icon { ... }
    // ...
}
```

## Next Steps

After Phase 10 completion:
1. Test all complications thoroughly
2. Test on different watch faces
3. Gather user feedback
4. Optimize based on feedback
5. Consider additional complications based on feedback

## Future Enhancements

### Additional Complications
- Budget progress complication (configurable)
- Goal progress complication (configurable)
- Spending today complication
- Transaction count complication
- Category spending complication

### Advanced Features
- Complication configuration (select budget/goal)
- Complication grouping
- Complication themes
- Complication animations

## Conclusion

Phase 10 will implement 6 Wear OS complications to provide financial information directly on the watch face. The implementation follows a phased approach, building infrastructure first, then implementing complications by category (action, information). Battery optimization is a key consideration throughout. The complications will enhance the user experience by providing at-a-glance information and quick actions without leaving the watch face. Shared components with tiles will reduce code duplication and improve maintainability.
