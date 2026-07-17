package com.jayanth.jmoney.wear.presentation.tiles.utils

import com.jayanth.jmoney.wear.data.database.TransactionDao
import com.jayanth.jmoney.wear.data.repository.AuthRepository
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TileDataFetcher @Inject constructor(
    private val transactionDao: TransactionDao,
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
}

data class RemainingData(
    val amount: Double,
    val isOverspent: Boolean
)
