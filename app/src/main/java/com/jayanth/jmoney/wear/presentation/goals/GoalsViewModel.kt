package com.jayanth.jmoney.wear.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jayanth.jmoney.wear.data.database.GoalDao
import com.jayanth.jmoney.wear.data.database.GoalEntity
import com.jayanth.jmoney.wear.data.repository.AuthRepository
import com.jayanth.jmoney.wear.data.repository.SyncRepository
import com.jayanth.jmoney.wear.data.repository.SyncState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoalItem(
    val goal: GoalEntity,
    val progressPercentage: Double,
    val isCompleted: Boolean,
    val remaining: Double
)

data class GoalsState(
    val goals: List<GoalItem> = emptyList(),
    val lastSyncTimestamp: Long = 0L,
    val isSyncing: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val goalDao: GoalDao,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val userId = authRepository.getCurrentUserId() ?: ""

    val goalsState: StateFlow<GoalsState> = if (userId.isEmpty()) {
        flowOf(GoalsState(isLoading = false))
    } else {
        combine(
            goalDao.getGoalsFlow(userId),
            syncRepository.syncState
        ) { goals, syncState ->
            val mappedGoals = goals.map { goal ->
                val progress = if (goal.goalAmount > 0) {
                    (goal.currentAmount / goal.goalAmount) * 100.0
                } else {
                    0.0
                }
                val remaining = Math.max(goal.goalAmount - goal.currentAmount, 0.0)
                val isCompleted = goal.currentAmount >= goal.goalAmount

                GoalItem(
                    goal = goal,
                    progressPercentage = progress,
                    isCompleted = isCompleted,
                    remaining = remaining
                )
            }

            GoalsState(
                goals = mappedGoals,
                lastSyncTimestamp = syncRepository.getLastSyncTimestamp(),
                isSyncing = syncState is SyncState.Syncing,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GoalsState()
    )

    fun syncNow() {
        viewModelScope.launch {
            syncRepository.runPartialSync()
        }
    }
}
