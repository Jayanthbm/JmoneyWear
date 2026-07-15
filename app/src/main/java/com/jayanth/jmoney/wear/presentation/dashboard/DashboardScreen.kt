package com.jayanth.jmoney.wear.presentation.dashboard

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Bolt
import com.jayanth.jmoney.wear.data.database.CategoryEntity
import com.jayanth.jmoney.wear.data.database.PayeeEntity
import com.jayanth.jmoney.wear.presentation.common.buttons.ActionButton
import com.jayanth.jmoney.wear.presentation.common.cards.MetricCard
import com.jayanth.jmoney.wear.presentation.common.cards.TransactionCard
import com.jayanth.jmoney.wear.presentation.common.loading.LoadingIndicator
import com.jayanth.jmoney.wear.presentation.form.TransactionFormData
import com.jayanth.jmoney.wear.presentation.form.TransactionForm
import com.jayanth.jmoney.wear.presentation.utils.formatCurrency
import com.jayanth.jmoney.wear.presentation.utils.formatRelativeTime
import com.jayanth.jmoney.wear.presentation.utils.formatTransactionDate

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.dashboardState.collectAsState()
    val listState = rememberTransformingLazyColumnState()
    val context = LocalContext.current

    // Location permissions state
    var locationPermissionGranted by remember {
        mutableStateOf(
            context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
            context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // Transaction Form Overlay State
    var showTransactionFormOverlay by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<android.location.Location?>(null) }

    // Request location permissions and fetch location when the transaction form is displayed
    LaunchedEffect(showTransactionFormOverlay, locationPermissionGranted) {
        if (showTransactionFormOverlay) {
            if (!locationPermissionGranted) {
                permissionLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            } else {
                try {
                    val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
                    
                    // Get last known location first (fast fallback)
                    val lastKnown = if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                        context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                            ?: locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                            ?: locationManager.getLastKnownLocation(android.location.LocationManager.PASSIVE_PROVIDER)
                    } else null
                    if (lastKnown != null) {
                        currentLocation = lastKnown
                    }

                    // Request a fresh location asynchronously (API 30+)
                    if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                        context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        val provider = if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                            android.location.LocationManager.GPS_PROVIDER
                        } else {
                            android.location.LocationManager.NETWORK_PROVIDER
                        }
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                            locationManager.getCurrentLocation(
                                provider,
                                null,
                                context.mainExecutor
                            ) { loc ->
                                if (loc != null) {
                                    currentLocation = loc
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    // ignore
                }
            }
        } else {
            currentLocation = null
        }
    }
    var transactionFormData by remember { 
        mutableStateOf(
            TransactionFormData(
                amount = 0.0,
                type = "Expense",
                category = null,
                payee = null,
                description = ""
            )
        )
    }

    // Quick Transaction presets overlay state
    var showQuickTransactionsOverlay by remember { mutableStateOf(false) }
    var activeQuickTxItem by remember { mutableStateOf<QuickTransactionItem?>(null) }

    // Default category names per type
    val defaultExpenseCategoryName = "General"
    val defaultIncomeCategoryName = "Salary"

    val launchCustomAdd = { type: String ->
        val defaultName = if (type == "Income") defaultIncomeCategoryName else defaultExpenseCategoryName
        val defaultCategory = state.categories.firstOrNull {
            it.type.equals(type, ignoreCase = true) && it.name.equals(defaultName, ignoreCase = true)
        } ?: state.categories.firstOrNull { it.type.equals(type, ignoreCase = true) }
        
        transactionFormData = TransactionFormData(
            amount = 0.0,
            type = type,
            category = defaultCategory,
            payee = null,
            description = ""
        )
        activeQuickTxItem = null
        showTransactionFormOverlay = true
    }

    // Back handlers
    BackHandler(enabled = showQuickTransactionsOverlay && !showTransactionFormOverlay) {
        showQuickTransactionsOverlay = false
    }
    BackHandler(enabled = showTransactionFormOverlay) {
        showTransactionFormOverlay = false
        activeQuickTxItem = null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        TransformingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Jmoney
            item {
                Text(
                    text = "Jmoney",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 18.dp, bottom = 6.dp)
                )
            }

            // Quick Add Action Buttons (3 Large Centered Icon Buttons)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActionButton(
                        onClick = { launchCustomAdd("Expense") },
                        icon = Icons.Filled.Remove,
                        text = "Expense",
                        isError = true
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    ActionButton(
                        onClick = { launchCustomAdd("Income") },
                        icon = Icons.Filled.Add,
                        text = "Income",
                        isPrimary = true
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    ActionButton(
                        onClick = { showQuickTransactionsOverlay = true },
                        icon = Icons.Filled.Bolt,
                        text = "Quick Transaction",
                        isPrimary = false
                    )
                }
            }

            // 1. Remaining For Period / Extra Spent for period
            item {
                MetricCard(
                    label = if (state.isOverspent) "EXTRA SPENT" else "REMAINING FOR PERIOD",
                    value = formatCurrency(state.remainingForPeriod),
                    isError = state.isOverspent
                )
            }

            // 2. Spent Today
            item {
                MetricCard(
                    label = "SPENT TODAY",
                    value = formatCurrency(state.spentToday)
                )
            }

            // 3. Upcoming Pay Day
            item {
                MetricCard(
                    label = "UPCOMING PAY DAY",
                    value = "${state.payDayInfo.remainingDays} days (${state.payDayInfo.nextPayDate})"
                )
            }

            // 4. Monthly Expense
            item {
                val isExpenseOverIncome = state.monthlyExpense > state.monthlyIncome
                MetricCard(
                    label = "MONTHLY EXPENSE",
                    value = formatCurrency(state.monthlyExpense),
                    isError = isExpenseOverIncome
                )
            }

            // 5. Monthly Income
            item {
                MetricCard(
                    label = "MONTHLY INCOME",
                    value = formatCurrency(state.monthlyIncome)
                )
            }

            // 6. Net Worth
            item {
                MetricCard(
                    label = "NET WORTH",
                    value = formatCurrency(state.netWorth)
                )
            }

            // 7. Recent Transactions
            if (state.recentTransactions.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        textAlign = TextAlign.Start
                    )
                }

                items(state.recentTransactions.size) { index ->
                    val tx = state.recentTransactions[index]
                    val displayName = when {
                        !tx.payeeName.isNullOrBlank() -> tx.payeeName
                        !tx.categoryName.isNullOrBlank() -> tx.categoryName
                        !tx.description.isNullOrBlank() -> tx.description
                        else -> "Transaction"
                    }
                    TransactionCard(
                        displayName = displayName,
                        date = formatTransactionDate(tx.date),
                        amount = tx.amount,
                        isIncome = tx.type == "Income"
                    )
                }
            }

            // 8. Refresh Sync
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 28.dp)
                ) {
                    if (state.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .height(20.dp)
                                .width(20.dp)
                                .padding(bottom = 4.dp)
                        )
                    } else {
                        Button(
                            onClick = { viewModel.syncNow() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = "Refresh Sync",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    Text(
                        text = formatRelativeTime(state.lastSyncTimestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Full Screen Quick Transactions Overlay
        if (showQuickTransactionsOverlay) {
            val quickTxs = state.quickTransactions
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (quickTxs.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No quick presets found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showQuickTransactionsOverlay = false }
                        ) {
                            Text("Back", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                } else {
                    TransformingLazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Text(
                                text = "Quick Presets",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(quickTxs.size) { index ->
                            val qt = quickTxs[index]
                            val logoText = qt.template.identifier?.takeIf { it.isNotBlank() }
                                ?: qt.template.name.take(2).uppercase()

                            Card(
                                onClick = {
                                    transactionFormData = TransactionFormData(
                                        amount = qt.template.amount ?: 0.0,
                                        type = qt.template.type,
                                        category = state.categories.find { it.id == qt.template.categoryId },
                                        payee = state.payees.find { it.id == qt.template.payeeId },
                                        description = qt.template.description ?: qt.template.name
                                    )
                                    activeQuickTxItem = qt

                                    showQuickTransactionsOverlay = false
                                    showTransactionFormOverlay = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val isIncome = qt.template.type == "Income"
                                    val accentColor = if (isIncome) Color(0xFF4ADE80) else Color(0xFFEF4444)
                                    val badgeBgColor = if (isIncome) Color(0x224ADE80) else Color(0x22EF4444)
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(badgeBgColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = logoText,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = accentColor,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = qt.template.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = qt.categoryName ?: "Uncategorized",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = (if (qt.template.type == "Income") "+" else "-") + formatCurrency(qt.template.amount ?: 0.0),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (qt.template.type == "Income") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { showQuickTransactionsOverlay = false },
                                colors = ButtonDefaults.filledTonalButtonColors(),
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                Text("Cancel", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        // Transaction Form Overlay
        if (showTransactionFormOverlay) {
            TransactionForm(
                initialData = transactionFormData,
                categories = state.categories,
                payees = state.payees,
                onConfirm = { formData ->
                    var lat: Double? = currentLocation?.latitude
                    var lon: Double? = currentLocation?.longitude
                    if (lat == null || lon == null) {
                        if (locationPermissionGranted) {
                            try {
                                val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
                                val lastKnown = if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                                    context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                    locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                                        ?: locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                                        ?: locationManager.getLastKnownLocation(android.location.LocationManager.PASSIVE_PROVIDER)
                                } else null
                                if (lastKnown != null) {
                                    lat = lastKnown.latitude
                                    lon = lastKnown.longitude
                                }
                            } catch (e: Exception) {
                                // ignore to avoid crash
                            }
                        }
                    }

                    viewModel.addTransaction(
                        amount = formData.amount,
                        type = formData.type,
                        categoryId = formData.category?.id,
                        categoryName = formData.category?.name,
                        categoryIcon = formData.category?.icon,
                        categoryAppIcon = formData.category?.appIcon,
                        payeeId = formData.payee?.id,
                        payeeName = formData.payee?.name,
                        payeeLogo = formData.payee?.logo,
                        description = formData.description,
                        productLink = activeQuickTxItem?.template?.productLink,
                        latitude = lat,
                        longitude = lon
                    )
                    showTransactionFormOverlay = false
                    val displayLabel = activeQuickTxItem?.template?.name ?: formData.category?.name ?: "Transaction"
                    Toast.makeText(context, "Logged ${formatCurrency(formData.amount)} under $displayLabel", Toast.LENGTH_LONG).show()
                },
                onCancel = {
                    showTransactionFormOverlay = false
                    activeQuickTxItem = null
                }
            )
        }
    }
}

@WearPreviewDevices
@Composable
fun DashboardScreenPreview() {
    DashboardScreen()
}
