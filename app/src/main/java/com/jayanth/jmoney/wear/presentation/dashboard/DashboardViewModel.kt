package com.jayanth.jmoney.wear.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jayanth.jmoney.wear.data.database.CategoryDao
import com.jayanth.jmoney.wear.data.database.CategoryEntity
import com.jayanth.jmoney.wear.data.database.PayeeDao
import com.jayanth.jmoney.wear.data.database.PayeeEntity
import com.jayanth.jmoney.wear.data.database.QuickTransactionDao
import com.jayanth.jmoney.wear.data.database.QuickTransactionEntity
import com.jayanth.jmoney.wear.data.database.TransactionDao
import com.jayanth.jmoney.wear.data.database.TransactionEntity
import com.jayanth.jmoney.wear.data.repository.AuthRepository
import com.jayanth.jmoney.wear.data.repository.SyncRepository
import com.jayanth.jmoney.wear.data.repository.SyncState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

data class PayDayInfo(
    val remainingDays: Int,
    val nextPayDate: String,
    val daysInMonth: Int
)

data class QuickTransactionItem(
    val template: QuickTransactionEntity,
    val categoryName: String?,
    val categoryIcon: String?,
    val categoryAppIcon: String?,
    val payeeName: String?,
    val payeeLogo: String?
)

data class DashboardState(
    val remainingForPeriod: Double = 0.0,
    val isOverspent: Boolean = false,
    val spentToday: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val netWorth: Double = 0.0,
    val payDayInfo: PayDayInfo = PayDayInfo(0, "", 30),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val quickTransactions: List<QuickTransactionItem> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val payees: List<PayeeEntity> = emptyList(),
    val lastSyncTimestamp: Long = 0L,
    val isSyncing: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val payeeDao: PayeeDao,
    private val quickTransactionDao: QuickTransactionDao,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val userId = authRepository.getCurrentUserId() ?: ""

    val dashboardState: StateFlow<DashboardState> = if (userId.isEmpty()) {
        flowOf(DashboardState())
    } else {
        val today = LocalDate.now()
        val monthStart = today.withDayOfMonth(1).toString()
        val monthEnd = today.withDayOfMonth(today.lengthOfMonth()).toString()

        // Payday calculation
        val daysInMonth = today.lengthOfMonth()
        val currentDay = today.dayOfMonth
        val remainingDays = daysInMonth - currentDay + 1
        val nextPayDateInstant = today.plusMonths(1).withDayOfMonth(1)
        val formattedNextMonth = nextPayDateInstant.format(DateTimeFormatter.ofPattern("MMM 01"))
        val payDayInfo = PayDayInfo(remainingDays, formattedNextMonth, daysInMonth)

        combine(
            transactionDao.getMonthlyIncomeFlow(userId, monthStart, monthEnd),
            transactionDao.getMonthlyExpenseFlow(userId, monthStart, monthEnd),
            transactionDao.getSpentTodayFlow(userId, today.toString()),
            transactionDao.getNetWorthFlow(userId),
            transactionDao.getTransactionsFlow(userId).map { it.take(5) },
            categoryDao.getCategoriesFlow(userId),
            payeeDao.getPayeesFlow(userId),
            quickTransactionDao.getQuickTransactionsFlow(userId),
            syncRepository.syncState
        ) { array ->
            val monthlyIncomeVal = array[0] as? Double
            val monthlyExpenseVal = array[1] as? Double
            val spentTodayVal = array[2] as? Double
            val netWorthVal = array[3] as? Double
            @Suppress("UNCHECKED_CAST")
            val recentTx = array[4] as? List<TransactionEntity> ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val cats = array[5] as? List<CategoryEntity> ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val payeesList = array[6] as? List<PayeeEntity> ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val quickTxs = array[7] as? List<QuickTransactionEntity> ?: emptyList()
            val syncState = array[8] as? SyncState ?: SyncState.Idle

            val income = monthlyIncomeVal ?: 0.0
            val expense = monthlyExpenseVal ?: 0.0
            val spentToday = spentTodayVal ?: 0.0
            val netWorth = netWorthVal ?: 0.0

            val remaining = income - expense
            val isOverspent = remaining < 0

            val enrichedQuickTxs = quickTxs.map { qt ->
                val cat = cats.find { it.id == qt.categoryId }
                val payee = payeesList.find { it.id == qt.payeeId }
                QuickTransactionItem(
                    template = qt,
                    categoryName = cat?.name,
                    categoryIcon = cat?.icon,
                    categoryAppIcon = cat?.appIcon,
                    payeeName = payee?.name,
                    payeeLogo = payee?.logo
                )
            }

            DashboardState(
                remainingForPeriod = Math.abs(remaining),
                isOverspent = isOverspent,
                spentToday = spentToday,
                monthlyExpense = expense,
                monthlyIncome = income,
                netWorth = netWorth,
                payDayInfo = payDayInfo,
                recentTransactions = recentTx,
                quickTransactions = enrichedQuickTxs,
                categories = cats,
                payees = payeesList,
                lastSyncTimestamp = syncRepository.getLastSyncTimestamp(),
                isSyncing = syncState is SyncState.Syncing
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardState()
    )

    fun addTransaction(
        amount: Double,
        type: String,
        categoryId: String?,
        categoryName: String?,
        categoryIcon: String?,
        categoryAppIcon: String?,
        payeeId: String?,
        payeeName: String?,
        payeeLogo: String?,
        description: String?,
        productLink: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            val newTx = TransactionEntity(
                id = UUID.randomUUID().toString(),
                amount = amount,
                description = description ?: "Logged via watch",
                transactionTimestamp = java.time.LocalDateTime.now().toString(),
                date = LocalDate.now().toString(),
                categoryId = categoryId,
                categoryName = categoryName,
                categoryIcon = categoryIcon,
                categoryAppIcon = categoryAppIcon,
                payeeId = payeeId,
                payeeName = payeeName,
                payeeLogo = payeeLogo,
                type = type,
                userId = userId,
                productLink = productLink,
                tid = 0,
                latitude = latitude,
                longitude = longitude,
                groupId = null,
                groupName = null,
                syncStatus = 1,
                createdAt = Instant.now().toString(),
                updatedAt = Instant.now().toString()
            )
            transactionDao.insertOrReplace(listOf(newTx))
            syncRepository.runPartialSync()
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            syncRepository.runPartialSync()
        }
    }
}
