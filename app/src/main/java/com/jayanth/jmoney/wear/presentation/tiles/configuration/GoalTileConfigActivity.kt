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
import com.jayanth.jmoney.wear.data.database.GoalDao
import com.jayanth.jmoney.wear.data.database.GoalEntity
import com.jayanth.jmoney.wear.data.repository.AuthRepository
import com.jayanth.jmoney.wear.presentation.theme.JmoneyTheme
import com.jayanth.jmoney.wear.presentation.tiles.GoalTileService
import com.jayanth.jmoney.wear.presentation.tiles.GoalTileService2
import com.jayanth.jmoney.wear.presentation.tiles.GoalTileService3
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import com.jayanth.jmoney.wear.presentation.common.buttons.ExpressiveButton

@AndroidEntryPoint
class GoalTileConfigActivity : ComponentActivity() {

    @Inject
    lateinit var goalDao: GoalDao

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val configKey = intent.getStringExtra("config_key")
        if (configKey == null) {
            finish()
            return
        }

        setContent {
            JmoneyTheme {
                val userId = authRepository.getCurrentUserId() ?: ""
                var goals by remember { mutableStateOf<List<GoalEntity>>(emptyList()) }
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(userId) {
                    if (userId.isNotEmpty()) {
                        goals = goalDao.getGoals(userId)
                    }
                    isLoading = false
                }

                val listState = rememberTransformingLazyColumnState()

                ScreenScaffold(
                    scrollState = listState,
                    scrollIndicator = {
                        ScrollIndicator(state = listState)
                    }
                ) { contentPadding ->
                    TransformingLazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        state = listState,
                        contentPadding = contentPadding,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Text(
                                text = "Select Goal",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 18.dp, bottom = 12.dp)
                            )
                        }

                        if (isLoading) {
                            item {
                                Text("Loading...")
                            }
                        } else if (goals.isEmpty()) {
                            item {
                                Text("No goals found.")
                            }
                        } else {
                            items(goals.size) { index ->
                                val goal = goals[index]
                                ExpressiveButton(
                                    onClick = {
                                        saveGoalSelection(configKey, goal.id)
                                        requestTileUpdate()
                                        finish()
                                    },
                                    text = goal.name,
                                    isPrimary = false,
                                    width = Dp.Unspecified,
                                    height = 52.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun saveGoalSelection(configKey: String, goalId: String) {
        val prefs = getSharedPreferences("goal_tile_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(configKey, goalId).apply()
    }

    private fun requestTileUpdate() {
        val updater = TileService.getUpdater(applicationContext)
        updater.requestUpdate(GoalTileService::class.java)
        updater.requestUpdate(GoalTileService2::class.java)
        updater.requestUpdate(GoalTileService3::class.java)
    }
}
