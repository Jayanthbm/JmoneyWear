package com.jayanth.jmoney.wear.presentation.budgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.jayanth.jmoney.wear.presentation.common.loading.LoadingIndicator
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.jayanth.jmoney.wear.presentation.utils.formatCurrency
import com.jayanth.jmoney.wear.presentation.utils.formatRelativeTime

@Composable
fun BudgetsScreen(
    viewModel: BudgetsViewModel = viewModel()
) {
    val state by viewModel.budgetsState.collectAsState()
    val listState = rememberTransformingLazyColumnState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (state.isLoading) {
            LoadingIndicator()
        } else if (state.budgets.isEmpty()) {
            Text(
                text = "No budgets found",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        } else {
            TransformingLazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                item {
                    Text(
                        text = "Budgets",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 18.dp, bottom = 12.dp)
                    )
                }

                // Budgets List
                items(state.budgets.size) { index ->
                    val item = state.budgets[index]
                    val isOverspent = item.isOverspent
                    val progressColor = if (isOverspent) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }

                    val cardContainerColor = if (isOverspent) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainer
                    }
                    val cardContentColor = if (isOverspent) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }

                    Card(
                        onClick = {},
                        colors = CardDefaults.cardColors(
                            containerColor = cardContainerColor,
                            contentColor = cardContentColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = item.budget.name,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${formatCurrency(item.spent)} of ${formatCurrency(item.budget.amount)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isOverspent) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Custom progress bar with Spent Progress and Today Indicator Line
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (isOverspent) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                    )
                            ) {
                                // Spent Progress
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction = Math.min(1.0f, item.spentPercentage.toFloat()))
                                        .fillMaxHeight()
                                        .background(progressColor)
                                )

                                // Today Indicator Line (if current month selected)
                                if (item.isCurrentMonth) {
                                    val todayFraction = (item.todayProgress / 100.0).toFloat()
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterStart)
                                            .fillMaxWidth(fraction = todayFraction)
                                            .fillMaxHeight()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.CenterEnd)
                                                .width(2.dp)
                                                .fillMaxHeight()
                                                .background(if (isOverspent) Color.White else MaterialTheme.colorScheme.onSurface)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.adviceText,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isOverspent) Color.White else Color(0xFF4ADE80),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }

                // Sync controls footer (at the end of Budgets Screen)
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
        }
    }
}

@WearPreviewDevices
@Composable
fun BudgetsScreenPreview() {
    BudgetsScreen()
}
