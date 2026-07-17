package com.jayanth.jmoney.wear.presentation.tiles.utils

import com.jayanth.jmoney.wear.data.database.TransactionDao
import com.jayanth.jmoney.wear.data.repository.AuthRepository
import java.time.LocalDate
import com.jayanth.jmoney.wear.data.database.BudgetDao
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TileDataFetcher @Inject constructor(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val authRepository: AuthRepository
) {
    fun isLoggedIn(): Boolean {
        return authRepository.getCurrentUserId() != null
    }

    suspend fun fetchRemainingForPeriod(): RemainingData {
        val userId = authRepository.getCurrentUserId() ?: return RemainingData(0.0, false)
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
        val userId = authRepository.getCurrentUserId() ?: return ExpenseData(0.0, "")
        val today = LocalDate.now()
        val monthStart = today.withDayOfMonth(1).toString()
        val monthEnd = today.withDayOfMonth(today.lengthOfMonth()).toString()

        val expense = transactionDao.getExpenseSummary(userId, monthStart, monthEnd) ?: 0.0
        val monthStr = today.format(java.time.format.DateTimeFormatter.ofPattern("LLL")).uppercase()
        return ExpenseData(expense, monthStr)
    }

    suspend fun fetchMonthlyIncome(): IncomeData {
        val userId = authRepository.getCurrentUserId() ?: return IncomeData(0.0, "")
        val today = LocalDate.now()
        val monthStart = today.withDayOfMonth(1).toString()
        val monthEnd = today.withDayOfMonth(today.lengthOfMonth()).toString()

        val income = transactionDao.getIncomeSummary(userId, monthStart, monthEnd) ?: 0.0
        val monthStr = today.format(java.time.format.DateTimeFormatter.ofPattern("LLL")).uppercase()
        return IncomeData(income, monthStr)
    }

    suspend fun fetchNetWorth(): Double {
        val userId = authRepository.getCurrentUserId() ?: return 0.0
        return transactionDao.getNetWorth(userId) ?: 0.0
    }

    suspend fun fetchBudgetSpending(budgetId: String): BudgetSpendingData? {
        val userId = authRepository.getCurrentUserId() ?: return null
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
