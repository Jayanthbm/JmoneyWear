package com.jayanth.jmoney.wear.presentation.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.jayanth.jmoney.wear.presentation.common.buttons.ExpressiveButton
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.ScrollIndicator
import com.jayanth.jmoney.wear.presentation.common.buttons.ExpressiveButton
import com.jayanth.jmoney.wear.presentation.common.cards.TransactionCard
import com.jayanth.jmoney.wear.presentation.common.loading.LoadingIndicator
import com.jayanth.jmoney.wear.presentation.utils.formatTransactionDate

@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel = viewModel()
) {
    val state by viewModel.transactionsState.collectAsState()
    val listState = rememberTransformingLazyColumnState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        if (state.isLoading) {
            LoadingIndicator()
        } else if (state.transactions.isEmpty()) {
            Text(
                text = "No transactions found",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            ScreenScaffold(
                scrollState = listState,
                scrollIndicator = {
                    ScrollIndicator(state = listState)
                }
            ) { contentPadding ->
                TransformingLazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = contentPadding,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    item {
                        Text(
                        text = "Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 18.dp, bottom = 12.dp)
                    )
                }

                // Show transactions list
                items(state.transactions.size) { index ->
                    val tx = state.transactions[index]
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

                // More button at the bottom
                if (state.hasMore) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        ExpressiveButton(
                            onClick = { viewModel.loadMore() },
                            text = "More",
                            isPrimary = true,
                            width = 120.dp,
                            height = 40.dp,
                            shape = ButtonDefaults.shape,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                } else {
                    item {
                        Spacer(modifier = Modifier.height(28.dp))
                    }
                }
            }
        }
    }
}
}
