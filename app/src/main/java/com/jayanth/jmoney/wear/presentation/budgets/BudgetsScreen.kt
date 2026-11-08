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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.ScrollIndicator
import com.jayanth.jmoney.wear.presentation.common.buttons.ExpressiveButton
import com.jayanth.jmoney.wear.presentation.common.cards.ExpressiveCard
import com.jayanth.jmoney.wear.presentation.common.progress.ExpressiveProgressBar
import com.jayanth.jmoney.wear.presentation.common.loading.LoadingIndicator
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.jayanth.jmoney.wear.presentation.utils.formatCurrency
import com.jayanth.jmoney.wear.presentation.utils.formatRelativeTime

import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.size
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Icon

@Composable
fun BudgetsScreen(
    viewModel: BudgetsViewModel = viewModel()
) {
    val state by viewModel.budgetsState.collectAsState()
    val listState = rememberTransformingLazyColumnState()
    val coroutineScope = rememberCoroutineScope()
    val showScrollToTop by remember {
        derivedStateOf { listState.canScrollBackward }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        if (state.isLoading) {
            LoadingIndicator()
        } else if (state.budgets.isEmpty()) {
            Text(
                text = "No budgets found",
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

                    ExpressiveCard(
                        onClick = {},
                        colors = CardDefaults.cardColors(
                            containerColor = cardContainerColor,
                            contentColor = cardContentColor
                        )
                    ) {
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

                        // Expressive Progress Bar
                        ExpressiveProgressBar(
                            progress = item.spentPercentage.toFloat(),
                            progressColor = progressColor,
                            trackColor = if (isOverspent) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            todayProgress = if (item.isCurrentMonth) (item.todayProgress / 100.0).toFloat() else null
                        )

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
                            ExpressiveButton(
                                onClick = { viewModel.syncNow() },
                                text = "Refresh",
                                isPrimary = true,
                                width = Dp.Unspecified,
                                height = 52.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
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

        AnimatedVisibility(
            visible = showScrollToTop,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        ) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                modifier = Modifier.size(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowUpward,
                    contentDescription = "Scroll to top",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@WearPreviewDevices
@Composable
fun BudgetsScreenPreview() {
    BudgetsScreen()
}
