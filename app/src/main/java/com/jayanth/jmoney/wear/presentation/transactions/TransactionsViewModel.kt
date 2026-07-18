package com.jayanth.jmoney.wear.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jayanth.jmoney.wear.data.database.TransactionDao
import com.jayanth.jmoney.wear.data.database.TransactionEntity
import com.jayanth.jmoney.wear.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class TransactionsState(
    val transactions: List<TransactionEntity> = emptyList(),
    val hasMore: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val transactionDao: TransactionDao
) : ViewModel() {

    private val userId = authRepository.getCurrentUserId() ?: ""
    private val limit = MutableStateFlow(5)

    fun loadMore() {
        limit.value += 5
    }

    val transactionsState: StateFlow<TransactionsState> = if (userId.isEmpty()) {
        flowOf(TransactionsState(isLoading = false))
    } else {
        combine(
            transactionDao.getTransactionsFlow(userId),
            limit
        ) { list, currentLimit ->
            TransactionsState(
                transactions = list.take(currentLimit),
                hasMore = list.size > currentLimit,
                isLoading = false
            )
        }
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionsState()
    )
}
