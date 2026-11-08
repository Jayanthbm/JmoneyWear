package com.jayanth.jmoney.wear.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.ScrollIndicator
import com.jayanth.jmoney.wear.presentation.common.buttons.ExpressiveButton
import com.jayanth.jmoney.wear.presentation.common.cards.InfoCard
import com.jayanth.jmoney.wear.presentation.common.loading.LoadingIndicator
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.jayanth.jmoney.wear.presentation.utils.formatRelativeTime

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
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val state by viewModel.settingsState.collectAsState()
    val listState = rememberTransformingLazyColumnState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val versionName = remember {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.PackageInfoFlags.of(0)
                ).versionName ?: "Unknown"
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
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
                    text = "Settings",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 18.dp, bottom = 12.dp)
                )
            }

            // User account info
            item {
                InfoCard(
                    title = "Account",
                    subtitle = state.userEmail ?: "Not logged in"
                )
            }

            // Last Sync info
            item {
                InfoCard(
                    title = "Last Sync",
                    subtitle = formatRelativeTime(state.lastSyncTimestamp)
                )
            }

            // Force Full Sync Action
            item(key = "force_full_sync_button") {
                Spacer(modifier = Modifier.height(8.dp))
                ExpressiveButton(
                    onClick = { viewModel.forceFullSync() },
                    text = if (state.isSyncing) "Syncing..." else "Force full sync",
                    isPrimary = true,
                    enabled = !state.isSyncing,
                    width = Dp.Unspecified,
                    height = 52.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                )
            }

            // Sync Status / Message
            if (state.isSyncing) {
                item(key = "sync_progress") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        LoadingIndicator()
                        state.syncMessage?.let {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else if (state.syncMessage != null) {
                item(key = "sync_message") {
                    Text(
                        text = state.syncMessage!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (state.syncMessage!!.startsWith("Sync failed")) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Sign Out Action
            item {
                Spacer(modifier = Modifier.height(4.dp))
                ExpressiveButton(
                    onClick = { viewModel.signOut() },
                    text = "Sign Out",
                    isPrimary = false,
                    isError = true,
                    enabled = !state.isSyncing,
                    width = Dp.Unspecified,
                    height = 52.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                )
            }

            // App Version Footer
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Jmoney Wear v$versionName",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
    }
}
}

@WearPreviewDevices
@Composable
fun SettingsScreenPreview() {
    SettingsScreen()
}
