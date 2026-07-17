package com.jayanth.jmoney.wear.presentation.tiles.configuration

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.tiles.TileService
import com.jayanth.jmoney.wear.data.database.BudgetDao
import com.jayanth.jmoney.wear.data.database.BudgetEntity
import com.jayanth.jmoney.wear.data.repository.AuthRepository
import com.jayanth.jmoney.wear.presentation.theme.JmoneyTheme
import com.jayanth.jmoney.wear.presentation.tiles.BudgetTileService
import com.jayanth.jmoney.wear.presentation.tiles.BudgetTileService2
import com.jayanth.jmoney.wear.presentation.tiles.BudgetTileService3
import com.jayanth.jmoney.wear.presentation.tiles.BudgetTileService4
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BudgetTileConfigActivity : ComponentActivity() {

    @Inject
    lateinit var budgetDao: BudgetDao

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Launched via tile tap — configKey identifies which BudgetTileService slot
        val configKey = intent.getStringExtra("config_key")
        if (configKey == null) {
            finish()
            return
        }

        setContent {
            JmoneyTheme {
                val userId = authRepository.getCurrentUserId() ?: ""
                var budgets by remember { mutableStateOf<List<BudgetEntity>>(emptyList()) }
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(userId) {
                    if (userId.isNotEmpty()) {
                        budgets = budgetDao.getBudgets(userId)
                    }
                    isLoading = false
                }

                val listState = rememberScalingLazyListState()

                ScalingLazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    state = listState
                ) {
                    item {
                        Text(
                            text = "Select Budget",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (isLoading) {
                        item {
                            Text("Loading...")
                        }
                    } else if (budgets.isEmpty()) {
                        item {
                            Text("No budgets found.")
                        }
                    } else {
                        items(budgets.size) { index ->
                            val budget = budgets[index]
                            Button(
                                onClick = {
                                    saveBudgetSelection(configKey, budget.id)
                                    requestTileUpdate()
                                    finish()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(budget.name)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun saveBudgetSelection(configKey: String, budgetId: String) {
        val prefs = getSharedPreferences("budget_tile_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(configKey, budgetId).apply()
    }

    private fun requestTileUpdate() {
        val updater = TileService.getUpdater(applicationContext)
        updater.requestUpdate(BudgetTileService::class.java)
        updater.requestUpdate(BudgetTileService2::class.java)
        updater.requestUpdate(BudgetTileService3::class.java)
        updater.requestUpdate(BudgetTileService4::class.java)
    }
}
