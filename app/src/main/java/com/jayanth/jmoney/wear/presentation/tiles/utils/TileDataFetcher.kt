package com.jayanth.jmoney.wear.presentation.tiles.utils

import com.jayanth.jmoney.wear.data.database.TransactionDao
import com.jayanth.jmoney.wear.data.repository.AuthRepository
import java.time.LocalDate
import com.jayanth.jmoney.wear.data.database.BudgetDao
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TileDataFetcher @Inject constructor(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val goalDao: com.jayanth.jmoney.wear.data.database.GoalDao,
    private val authRepository: AuthRepository
) {
    private val sessionRestoreMutex = Mutex()

    /**
     * Tile providers can be started by the Wear OS host before MainActivity.
     * Restore the persisted Supabase session here instead of interpreting an
     * uninitialized in-memory session as a signed-out user.
     */
    private suspend fun authenticatedUserId(): String? {
        authRepository.getCurrentUserId()?.let { return it }

        return sessionRestoreMutex.withLock {
            authRepository.getCurrentUserId()?.let { return@withLock it }
            if (authRepository.autoLogin()) {
                authRepository.getCurrentUserId()
            } else {
                null
            }
        }
    }

    suspend fun isLoggedIn(): Boolean = authenticatedUserId() != null

    suspend fun fetchGoalProgress(goalId: String): GoalProgressData? {
        val userId = authenticatedUserId() ?: return null
        val goals = goalDao.getGoals(userId)
        val goal = goals.firstOrNull { it.id == goalId } ?: return null

        val pct = if (goal.goalAmount > 0) (goal.currentAmount / goal.goalAmount) else 0.0

        return GoalProgressData(
            name = goal.name,
            saved = goal.currentAmount,
            target = goal.goalAmount,
            percentage = pct
        )
    }

    suspend fun fetchRemainingForPeriod(): RemainingData {
        val userId = authenticatedUserId() ?: return RemainingData(0.0, false)
        val today = LocalDate.now()
        val monthStart = today.withDayOfMonth(1).toString()
        val monthEnd = today.withDayOfMonth(today.lengthOfMonth()).toString()

        val income = transactionDao.getIncomeSummary(userId, monthStart, monthEnd) ?: 0.0
        val expense = transactionDao.getExpenseSummary(userId, monthStart, monthEnd) ?: 0.0

        val remaining = income - expense
        return RemainingData(
            amount = Math.abs(remaining),
            isOverspent = remaining < 0
        )
    }

    suspend fun fetchMonthlyExpense(): ExpenseData {
        val userId = authenticatedUserId() ?: return ExpenseData(0.0, "")
        val today = LocalDate.now()
        val monthStart = today.withDayOfMonth(1).toString()
        val monthEnd = today.withDayOfMonth(today.lengthOfMonth()).toString()

        val expense = transactionDao.getExpenseSummary(userId, monthStart, monthEnd) ?: 0.0
        val monthStr = today.format(java.time.format.DateTimeFormatter.ofPattern("LLL")).uppercase()
        return ExpenseData(expense, monthStr)
    }

    suspend fun fetchMonthlyIncome(): IncomeData {
        val userId = authenticatedUserId() ?: return IncomeData(0.0, "")
        val today = LocalDate.now()
        val monthStart = today.withDayOfMonth(1).toString()
        val monthEnd = today.withDayOfMonth(today.lengthOfMonth()).toString()

        val income = transactionDao.getIncomeSummary(userId, monthStart, monthEnd) ?: 0.0
        val monthStr = today.format(java.time.format.DateTimeFormatter.ofPattern("LLL")).uppercase()
        return IncomeData(income, monthStr)
    }

    suspend fun fetchNetWorth(): Double {
        val userId = authenticatedUserId() ?: return 0.0
        return transactionDao.getNetWorth(userId) ?: 0.0
    }

    suspend fun fetchBudgetSpending(budgetId: String): BudgetSpendingData? {
        val userId = authenticatedUserId() ?: return null
        val budgets = budgetDao.getBudgets(userId)
        val budget = budgets.firstOrNull { it.id == budgetId } ?: return null

        val today = LocalDate.now()
        val monthStart = today.withDayOfMonth(1).toString()
        val monthEnd = today.withDayOfMonth(today.lengthOfMonth()).toString()

        val categoryIds = try {
            Json.decodeFromString<List<String>>(budget.categories)
        } catch (e: Exception) {
            emptyList()
        }

        val spent = if (categoryIds.isNotEmpty()) {
            transactionDao.getBudgetSpending(userId, categoryIds, monthStart, monthEnd) ?: 0.0
        } else 0.0

        val remaining = budget.amount - spent
        val isOverspent = remaining < 0
        val percentage = if (budget.amount > 0) (spent / budget.amount) else 0.0

        val daysInMonth = today.lengthOfMonth()
        val currentDay = today.dayOfMonth
        val remainingDays = daysInMonth - currentDay + 1

        val advice = if (isOverspent) {
            val overspentVal = spent - budget.amount
            "Overspent ₹${String.format("%.0f", overspentVal)}"
        } else {
            val perDay = if (remainingDays > 0) remaining / remainingDays else remaining
            "₹${String.format("%.0f", perDay)}/day left"
        }

        return BudgetSpendingData(
            name = budget.name,
            spent = spent,
            limit = budget.amount,
            percentage = percentage,
            advice = advice,
            isOverspent = isOverspent
        )
    }
}

data class RemainingData(
    val amount: Double,
    val isOverspent: Boolean
)

data class ExpenseData(
    val amount: Double,
    val month: String
)

data class IncomeData(
    val amount: Double,
    val month: String
)

data class BudgetSpendingData(
    val name: String,
    val spent: Double,
    val limit: Double,
    val percentage: Double,
    val advice: String,
    val isOverspent: Boolean
)

data class GoalProgressData(
    val name: String,
    val saved: Double,
    val target: Double,
    val percentage: Double
)
