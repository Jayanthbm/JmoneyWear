package com.jayanth.jmoney.wear.presentation.dashboard

import android.app.RemoteInput
import android.text.InputType
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import androidx.wear.input.RemoteInputIntentHelper
import com.jayanth.jmoney.wear.data.database.CategoryEntity
import com.jayanth.jmoney.wear.data.database.PayeeEntity
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

    // Unified Transaction Form Overlay States
    var showTransactionFormOverlay by remember { mutableStateOf(false) }
    var transactionAmount by remember { mutableStateOf(0.0) }
    var transactionType by remember { mutableStateOf("Expense") }
    var selectedCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var selectedPayee by remember { mutableStateOf<PayeeEntity?>(null) }
    var transactionDescription by remember { mutableStateOf("") }
    var activeQuickTxItem by remember { mutableStateOf<QuickTransactionItem?>(null) }

    // Quick Transaction presets overlay state
    var showQuickTransactionsOverlay by remember { mutableStateOf(false) }

    // Secondary selector lists overlay states
    var showCategorySelection by remember { mutableStateOf(false) }
    var showPayeeSelection by remember { mutableStateOf(false) }

    // Launchers for Amount & Description inputs
    val amountInputLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val results = RemoteInput.getResultsFromIntent(result.data)
        val amountStr = results?.getCharSequence("amount")?.toString()
        val amount = amountStr?.toDoubleOrNull()
        if (amount != null && amount > 0.0) {
            transactionAmount = amount
        } else if (amountStr != null) {
            Toast.makeText(context, "Invalid amount", Toast.LENGTH_SHORT).show()
        }
    }

    // Amount input: forces keyboard-only with numeric input type
    val launchAmountInput = {
        val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
        val remoteInput = RemoteInput.Builder("amount")
            .setLabel("Enter Amount (₹)")
            .setAllowFreeFormInput(true)
            .build()
        RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(remoteInput))
        // Force numeric keyboard only (no emoji, voice opens keyboard with number mode)
        intent.putExtra("android.text.InputType",
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        amountInputLauncher.launch(intent)
    }

    val descInputLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val results = RemoteInput.getResultsFromIntent(result.data)
        val desc = results?.getCharSequence("description")?.toString()
        if (desc != null) {
            transactionDescription = desc
        }
    }

    val launchDescInput = {
        val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
        val remoteInputs = listOf(
            RemoteInput.Builder("description")
                .setLabel("Description (optional)")
                .build()
        )
        RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs)
        descInputLauncher.launch(intent)
    }

    // Default category names per type
    val defaultExpenseCategoryName = "General"
    val defaultIncomeCategoryName = "Salary"

    val launchCustomAdd = { type: String ->
        transactionType = type
        transactionAmount = 0.0
        val defaultName = if (type == "Income") defaultIncomeCategoryName else defaultExpenseCategoryName
        selectedCategory = state.categories.firstOrNull {
            it.type.equals(type, ignoreCase = true) && it.name.equals(defaultName, ignoreCase = true)
        } ?: state.categories.firstOrNull { it.type.equals(type, ignoreCase = true) }
        selectedPayee = null
        transactionDescription = ""
        activeQuickTxItem = null
        showTransactionFormOverlay = true
    }

    // Back handler priority: sub-overlays > form > presets
    // Each is registered separately so innermost (last-composed) takes priority
    BackHandler(enabled = showQuickTransactionsOverlay && !showTransactionFormOverlay) {
        showQuickTransactionsOverlay = false
    }
    BackHandler(enabled = showTransactionFormOverlay && !showCategorySelection && !showPayeeSelection) {
        showTransactionFormOverlay = false
        activeQuickTxItem = null
    }
    BackHandler(enabled = showCategorySelection) {
        showCategorySelection = false
    }
    BackHandler(enabled = showPayeeSelection) {
        showPayeeSelection = false
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
                    Button(
                        onClick = { launchCustomAdd("Expense") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Text(
                            text = "➖",
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = { launchCustomAdd("Income") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Text(
                            text = "➕",
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = { showQuickTransactionsOverlay = true },
                        colors = ButtonDefaults.filledTonalButtonColors(),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Text(
                            text = "⚡",
                            fontSize = 20.sp
                        )
                    }
                }
            }

            // 1. Remaining For Period / Extra Spent for period
            item {
                val cardColor = if (state.isOverspent) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                }
                val textColor = if (state.isOverspent) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                }
                Card(
                    onClick = {},
                    colors = CardDefaults.cardColors(
                        containerColor = cardColor,
                        contentColor = textColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (state.isOverspent) "EXTRA SPENT" else "REMAINING FOR PERIOD",
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatCurrency(state.remainingForPeriod),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }

            // 2. Spent Today
            item {
                Card(
                    onClick = {},
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "SPENT TODAY",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatCurrency(state.spentToday),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // 3. Upcoming Pay Day
            item {
                Card(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "UPCOMING PAY DAY",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${state.payDayInfo.remainingDays} days (${state.payDayInfo.nextPayDate})",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // 4. Monthly Expense
            item {
                val isExpenseOverIncome = state.monthlyExpense > state.monthlyIncome
                val expenseContainerColor = if (isExpenseOverIncome) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                }
                val expenseContentColor = if (isExpenseOverIncome) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
                Card(
                    onClick = {},
                    colors = CardDefaults.cardColors(
                        containerColor = expenseContainerColor,
                        contentColor = expenseContentColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "MONTHLY EXPENSE",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isExpenseOverIncome) expenseContentColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatCurrency(state.monthlyExpense),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // 5. Monthly Income
            item {
                Card(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "MONTHLY INCOME",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatCurrency(state.monthlyIncome),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // 6. Net Worth
            item {
                Card(
                    onClick = {},
                    border = CardDefaults.outlinedCardBorder(),
                    colors = CardDefaults.outlinedCardColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "NET WORTH",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatCurrency(state.netWorth),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
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
                    Card(
                        onClick = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                val displayName = when {
                                    !tx.payeeName.isNullOrBlank() -> tx.payeeName
                                    !tx.categoryName.isNullOrBlank() -> tx.categoryName
                                    !tx.description.isNullOrBlank() -> tx.description
                                    else -> "Transaction"
                                }
                                Text(
                                    text = displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = formatTransactionDate(tx.date),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = (if (tx.type == "Income") "+" else "-") + formatCurrency(tx.amount),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (tx.type == "Income") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
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
                                    transactionType = qt.template.type
                                    transactionAmount = qt.template.amount ?: 0.0
                                    selectedCategory = state.categories.find { it.id == qt.template.categoryId }
                                    selectedPayee = state.payees.find { it.id == qt.template.payeeId }
                                    transactionDescription = qt.template.description ?: qt.template.name
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

        // Consolidated Add/Confirm Transaction Form Overlay
        if (showTransactionFormOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                TransformingLazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        val headerTitle = when {
                            activeQuickTxItem != null -> "Confirm Preset"
                            transactionType == "Income" -> "Log Income"
                            else -> "Log Expense"
                        }
                        Text(
                            text = headerTitle,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // 1. Amount Card (Tap to Input numeric amount)
                    item {
                        Card(
                            onClick = { launchAmountInput() },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Amount (Tap to Edit)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = formatCurrency(transactionAmount),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }

                    // 2. Category Card (Tap to select)
                    item {
                        Card(
                            onClick = { showCategorySelection = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Category (Tap to Edit)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = selectedCategory?.name ?: "Tap to Select",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (selectedCategory == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // 3. Payee Card (Tap to select)
                    item {
                        Card(
                            onClick = { showPayeeSelection = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Payee (Tap to Edit)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = selectedPayee?.name ?: "None",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (selectedPayee == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // 4. Description Card (Tap to type)
                    item {
                        Card(
                            onClick = { launchDescInput() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Description (Tap to Edit)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (transactionDescription.isBlank()) "(empty)" else transactionDescription,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (transactionDescription.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Action Buttons (OK / Cancel)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showTransactionFormOverlay = false },
                                colors = ButtonDefaults.filledTonalButtonColors(),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel", style = MaterialTheme.typography.labelSmall)
                            }
                            Button(
                                onClick = {
                                    if (transactionAmount <= 0.0) {
                                        Toast.makeText(context, "Please enter an amount > 0", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.addTransaction(
                                            amount = transactionAmount,
                                            type = transactionType,
                                            categoryId = selectedCategory?.id,
                                            categoryName = selectedCategory?.name,
                                            categoryIcon = selectedCategory?.icon,
                                            categoryAppIcon = selectedCategory?.appIcon,
                                            payeeId = selectedPayee?.id,
                                            payeeName = selectedPayee?.name,
                                            payeeLogo = selectedPayee?.logo,
                                            description = transactionDescription,
                                            productLink = activeQuickTxItem?.template?.productLink
                                        )
                                        showTransactionFormOverlay = false
                                        val displayLabel = activeQuickTxItem?.template?.name ?: selectedCategory?.name ?: "Transaction"
                                        Toast.makeText(context, "Logged ${formatCurrency(transactionAmount)} under $displayLabel", Toast.LENGTH_LONG).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("OK", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        // Full Screen Category Selection List (Filtered by transactionType)
        if (showCategorySelection) {
            val filteredCategories = state.categories.filter { it.type.equals(transactionType, ignoreCase = true) }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (filteredCategories.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No $transactionType categories found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showCategorySelection = false }
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
                                text = "Select Category",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(filteredCategories.size) { index ->
                            val category = filteredCategories[index]
                            Card(
                                onClick = {
                                    selectedCategory = category
                                    showCategorySelection = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp)
                            ) {
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { showCategorySelection = false },
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

        // Full Screen Payee Selection List
        if (showPayeeSelection) {
            val payees = state.payees
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                TransformingLazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Text(
                            text = "Select Payee",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    // "None" option to remove/skip payee assignment
                    item {
                        Card(
                            onClick = {
                                selectedPayee = null
                                showPayeeSelection = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = "None",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    items(payees.size) { index ->
                        val payee = payees[index]
                        Card(
                            onClick = {
                                selectedPayee = payee
                                showPayeeSelection = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = payee.name,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showPayeeSelection = false },
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
}

@WearPreviewDevices
@Composable
fun DashboardScreenPreview() {
    DashboardScreen()
}
