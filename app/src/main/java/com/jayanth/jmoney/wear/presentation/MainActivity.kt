package com.jayanth.jmoney.wear.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.activity.compose.BackHandler
import com.jayanth.jmoney.wear.presentation.auth.AuthState
import com.jayanth.jmoney.wear.presentation.auth.LoginScreen
import com.jayanth.jmoney.wear.presentation.auth.LoginViewModel
import com.jayanth.jmoney.wear.presentation.budgets.BudgetsScreen
import com.jayanth.jmoney.wear.presentation.goals.GoalsScreen
import com.jayanth.jmoney.wear.presentation.settings.SettingsScreen
import com.jayanth.jmoney.wear.presentation.dashboard.DashboardScreen
import com.jayanth.jmoney.wear.presentation.transactions.TransactionsScreen
import com.jayanth.jmoney.wear.presentation.dashboard.DashboardViewModel
import com.jayanth.jmoney.wear.presentation.sync.SyncScreen
import com.jayanth.jmoney.wear.presentation.theme.JmoneyTheme
import com.jayanth.jmoney.wear.data.repository.SyncRepository
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.HorizontalPagerScaffold
import androidx.wear.compose.material3.MaterialTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.jayanth.jmoney.wear.data.work.SyncScheduler

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val loginViewModel: LoginViewModel by viewModels()

    @Inject
    lateinit var syncRepository: SyncRepository

    private val actionState = androidx.compose.runtime.mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SyncScheduler.schedule(this)
        actionState.value = intent?.getStringExtra("action")
        setContent {
            WearApp(
                loginViewModel = loginViewModel,
                syncRepository = syncRepository,
                initialAction = actionState.value,
                onConsumeAction = { actionState.value = null }
            )
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        actionState.value = intent.getStringExtra("action")
    }
}

@Composable
fun WearApp(
    loginViewModel: LoginViewModel,
    syncRepository: SyncRepository,
    initialAction: String?,
    onConsumeAction: () -> Unit
) {
    JmoneyTheme {
        AppScaffold {
            val authState by loginViewModel.authState.collectAsState()
            var hasSynced by remember { mutableStateOf(true) } // Default to true while loading

            LaunchedEffect(authState, hasSynced) {
                if (authState is AuthState.Authenticated) {
                    hasSynced = syncRepository.hasSyncedBefore()
                }
            }

            when (authState) {
                is AuthState.Authenticated -> {
                    if (!hasSynced) {
                        SyncScreen(
                            onSyncSuccess = {
                                hasSynced = true
                            }
                        )
                    } else {
                        val pagerState = rememberPagerState(pageCount = { 5 })
                        val coroutineScope = rememberCoroutineScope()
                        var isOverlayOpen by remember { mutableStateOf(false) }

                        // Only intercept back when not on page 0 — dashboard handles its own overlays
                        BackHandler(enabled = pagerState.currentPage != 0) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        }

                        HorizontalPagerScaffold(pagerState = pagerState) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize(),
                                userScrollEnabled = !isOverlayOpen
                            ) { page ->
                                when (page) {
                                    0 -> DashboardScreen(
                                        initialAction = initialAction,
                                        onConsumeAction = onConsumeAction,
                                        onOverlayStateChanged = { isOverlayOpen = it }
                                    )
                                    1 -> TransactionsScreen()
                                    2 -> BudgetsScreen()
                                    3 -> GoalsScreen()
                                    4 -> SettingsScreen()
                                }
                            }
                        }
                    }
                }
                is AuthState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    LoginScreen(
                        onLoginSuccess = {}, // Auth state changes will automatically trigger recomposition
                        viewModel = loginViewModel
                    )
                }
            }
        }
    }
}