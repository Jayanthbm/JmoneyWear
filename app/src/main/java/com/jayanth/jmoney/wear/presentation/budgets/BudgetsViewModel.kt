package com.jayanth.jmoney.wear.presentation.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jayanth.jmoney.wear.data.database.BudgetDao
import com.jayanth.jmoney.wear.data.database.BudgetEntity
import com.jayanth.jmoney.wear.data.database.TransactionDao
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
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class BudgetSpendingItem(
    val budget: BudgetEntity,
    val spent: Double,
    val remaining: Double,
    val spentPercentage: Double,
    val isOverspent: Boolean,
    val adviceText: String,
    val todayProgress: Double,
    val daysInMonth: Int,
    val isCurrentMonth: Boolean
)

data class BudgetsState(
    val budgets: List<BudgetSpendingItem> = emptyList(),
    val lastSyncTimestamp: Long = 0L,
    val isSyncing: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val userId = authRepository.getCurrentUserId() ?: ""

    val budgetsState: StateFlow<BudgetsState> = if (userId.isEmpty()) {
        flowOf(BudgetsState(isLoading = false))
    } else {
        val today = LocalDate.now()
        val monthStart = today.withDayOfMonth(1)
        val monthEnd = today.withDayOfMonth(today.lengthOfMonth())

        // Elapsed month calculations
        val daysInMonth = today.lengthOfMonth()
        val currentDay = today.dayOfMonth
        val remainingDays = daysInMonth - currentDay + 1
        val todayProgress = (currentDay.toDouble() / daysInMonth.toDouble()) * 100.0

        combine(
            budgetDao.getBudgetsFlow(userId),
            transactionDao.getTransactionsForDateRangeFlow(userId, monthStart.toString(), monthEnd.toString()),
            syncRepository.syncState
        ) { budgets, transactions, syncState ->
            val enrichedList = budgets.map { budget ->
                val categoryIds = try {
                    Json.decodeFromString<List<String>>(budget.categories)
                } catch (e: Exception) {
                    emptyList()
                }

                val spent = transactions
                    .filter { it.type == "Expense" && it.categoryId in categoryIds }
                    .sumOf { it.amount }

                val remaining = budget.amount - spent
                val isOverspent = remaining < 0
                val spentPercentage = if (budget.amount > 0) (spent / budget.amount) else 0.0

                // React Native matching advice text logic
                val advice = if (isOverspent) {
                    val overspentVal = spent - budget.amount
                    "Overspent ₹${String.format("%.0f", overspentVal)}"
                } else {
                    val perDay = if (remainingDays > 0) remaining / remainingDays else remaining
                    "You can spend ₹${String.format("%.0f", perDay)}/day for $remainingDays more days"
                }

                BudgetSpendingItem(
                    budget = budget,
                    spent = spent,
                    remaining = remaining,
                    spentPercentage = spentPercentage,
                    isOverspent = isOverspent,
                    adviceText = advice,
                    todayProgress = todayProgress,
                    daysInMonth = daysInMonth,
                    isCurrentMonth = true
                )
            }

            BudgetsState(
                budgets = enrichedList,
                lastSyncTimestamp = syncRepository.getLastSyncTimestamp(),
                isSyncing = syncState is SyncState.Syncing,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BudgetsState()
    )

    fun syncNow() {
        viewModelScope.launch {
            syncRepository.runPartialSync()
        }
    }
}
