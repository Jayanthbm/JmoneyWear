package com.jayanth.jmoney.wear.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.jayanth.jmoney.wear.data.database.AppDatabase
import com.jayanth.jmoney.wear.data.database.BudgetDao
import com.jayanth.jmoney.wear.data.database.BudgetEntity
import com.jayanth.jmoney.wear.data.database.CategoryDao
import com.jayanth.jmoney.wear.data.database.CategoryEntity
import com.jayanth.jmoney.wear.data.database.GoalDao
import com.jayanth.jmoney.wear.data.database.GoalEntity
import com.jayanth.jmoney.wear.data.database.PayeeDao
import com.jayanth.jmoney.wear.data.database.PayeeEntity
import com.jayanth.jmoney.wear.data.database.QuickTransactionDao
import com.jayanth.jmoney.wear.data.database.QuickTransactionEntity
import com.jayanth.jmoney.wear.data.database.TransactionDao
import com.jayanth.jmoney.wear.data.database.TransactionEntity
import com.jayanth.jmoney.wear.data.database.TransactionGroupDao
import com.jayanth.jmoney.wear.data.database.TransactionGroupEntity
import com.jayanth.jmoney.wear.data.network.dto.SupabaseBudget
import com.jayanth.jmoney.wear.data.network.dto.SupabaseCategory
import com.jayanth.jmoney.wear.data.network.dto.SupabaseGoal
import com.jayanth.jmoney.wear.data.network.dto.SupabaseGroup
import com.jayanth.jmoney.wear.data.network.dto.SupabasePayee
import com.jayanth.jmoney.wear.data.network.dto.SupabaseQuickTransaction
import com.jayanth.jmoney.wear.data.network.dto.SupabaseTransaction
import com.jayanth.jmoney.wear.data.sync.*
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val authRepository: AuthRepository,
    private val db: AppDatabase,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val payeeDao: PayeeDao,
    private val quickTransactionDao: QuickTransactionDao,
    private val budgetDao: BudgetDao,
    private val goalDao: GoalDao,
    private val groupDao: TransactionGroupDao,
    private val transactionSync: TransactionSync,
    private val categorySync: CategorySync,
    private val payeeSync: PayeeSync,
    private val budgetSync: BudgetSync,
    private val goalSync: GoalSync,
    private val quickTransactionSync: QuickTransactionSync,
    private val transactionGroupSync: TransactionGroupSync,
    @ApplicationContext private val context: Context
) : SyncRepository {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    override val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val sharedPrefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)

    override suspend fun runFullSync(): Result<Unit> {
        val userId = authRepository.getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
        return runSync(userId, isPartial = false)
    }

    override suspend fun runPartialSync(): Result<Unit> {
        val userId = authRepository.getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
        return runSync(userId, isPartial = true)
    }

    private suspend fun runSync(userId: String, isPartial: Boolean): Result<Unit> {
        return runCatching {
            _syncState.value = SyncState.Syncing("Pushing local changes...")
            pushLocalChanges(userId)

            _syncState.value = SyncState.Syncing("Syncing Transactions...")
            transactionSync.syncRemoteChanges(userId, isPartial)

            _syncState.value = SyncState.Syncing("Syncing Categories...")
            categorySync.syncRemoteChanges(userId)

            _syncState.value = SyncState.Syncing("Syncing Payees...")
            payeeSync.syncRemoteChanges(userId)

            _syncState.value = SyncState.Syncing("Syncing Transaction Groups...")
            transactionGroupSync.syncRemoteChanges(userId)

            _syncState.value = SyncState.Syncing("Syncing Budgets...")
            budgetSync.syncRemoteChanges(userId)

            _syncState.value = SyncState.Syncing("Syncing Goals...")
            goalSync.syncRemoteChanges(userId)

            _syncState.value = SyncState.Syncing("Syncing Quick Transactions...")
            quickTransactionSync.syncRemoteChanges(userId)

            sharedPrefs.edit().putLong("last_sync_timestamp_$userId", System.currentTimeMillis()).apply()
            _syncState.value = SyncState.Success
        }.onFailure { error ->
            _syncState.value = SyncState.Error(error.localizedMessage ?: "Sync failed")
        }
    }

    override suspend fun resetLocalData(): Result<Unit> {
        val userId = authRepository.getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
        return runCatching {
            db.withTransaction {
                transactionDao.deleteAllForUser(userId)
                categoryDao.deleteAllForUser(userId)
                payeeDao.deleteAllForUser(userId)
                quickTransactionDao.deleteAllForUser(userId)
                budgetDao.deleteAllForUser(userId)
                goalDao.deleteAllForUser(userId)
                groupDao.deleteAllForUser(userId)
            }
            sharedPrefs.edit().remove("last_sync_timestamp_$userId").apply()
            _syncState.value = SyncState.Idle
        }
    }

    override suspend fun hasSyncedBefore(): Boolean {
        val userId = authRepository.getCurrentUserId() ?: return false
        return sharedPrefs.getLong("last_sync_timestamp_$userId", 0L) > 0L
    }

    override fun getLastSyncTimestamp(): Long {
        val userId = authRepository.getCurrentUserId() ?: return 0L
        return sharedPrefs.getLong("last_sync_timestamp_$userId", 0L)
    }

    private suspend fun pushLocalChanges(userId: String) {
        transactionSync.pushLocalChanges(userId)
        categorySync.pushLocalChanges(userId)
        payeeSync.pushLocalChanges(userId)
        transactionGroupSync.pushLocalChanges(userId)
        budgetSync.pushLocalChanges(userId)
        goalSync.pushLocalChanges(userId)
        quickTransactionSync.pushLocalChanges(userId)
    }

}
