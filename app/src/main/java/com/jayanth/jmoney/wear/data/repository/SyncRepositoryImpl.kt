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
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class TidResponse(val tid: Int)

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
            syncTransactions(userId, isPartial)

            _syncState.value = SyncState.Syncing("Syncing Categories...")
            syncCategories(userId)

            _syncState.value = SyncState.Syncing("Syncing Payees...")
            syncPayees(userId)

            _syncState.value = SyncState.Syncing("Syncing Transaction Groups...")
            syncGroups(userId)

            _syncState.value = SyncState.Syncing("Syncing Budgets...")
            syncBudgets(userId)

            _syncState.value = SyncState.Syncing("Syncing Goals...")
            syncGoals(userId)

            _syncState.value = SyncState.Syncing("Syncing Quick Transactions...")
            syncQuickTransactions(userId)

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
        pushLocalTransactions(userId)
        pushLocalCategories(userId)
        pushLocalPayees(userId)
        pushLocalGroups(userId)
        pushLocalBudgets(userId)
        pushLocalGoals(userId)
        pushLocalQuickTransactions(userId)
    }

    private suspend fun pushLocalTransactions(userId: String) {
        val unsynced = transactionDao.getUnsyncedTransactions(userId)
        for (tx in unsynced) {
            if (tx.deleted == 1) {
                try {
                    supabaseClient.from("transactions").delete {
                        filter { eq("id", tx.id) }
                    }
                    transactionDao.delete(tx.id)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                val payload = SupabaseTransaction(
                    id = tx.id,
                    amount = tx.amount,
                    description = tx.description,
                    transactionTimestamp = toSupabaseTransactionTimestamp(tx.transactionTimestamp),
                    categoryId = tx.categoryId,
                    payeeId = tx.payeeId,
                    groupId = tx.groupId,
                    type = tx.type,
                    userId = tx.userId,
                    productLink = tx.productLink,
                    tid = tx.tid,
                    latitude = tx.latitude,
                    longitude = tx.longitude
                )
                try {
                    val result = supabaseClient.from("transactions").upsert(payload) {
                        select(Columns.list("tid"))
                    }.decodeSingle<TidResponse>()
                    transactionDao.updateTidAndSyncStatus(tx.id, result.tid)
                } catch (e: Exception) {
                    supabaseClient.from("transactions").upsert(payload)
                    transactionDao.updateSyncStatus(tx.id, 0)
                }
            }
        }
    }

    private suspend fun pushLocalCategories(userId: String) {
        val unsynced = categoryDao.getUnsyncedCategories(userId)
        for (cat in unsynced) {
            val payload = SupabaseCategory(
                id = cat.id,
                name = cat.name,
                type = cat.type,
                icon = cat.icon,
                appIcon = cat.appIcon,
                userId = cat.userId,
                isLivingCost = cat.isLivingCost,
                priority = cat.priority
            )
            try {
                supabaseClient.from("categories").upsert(payload)
                categoryDao.updateSyncStatus(cat.id, 0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun pushLocalPayees(userId: String) {
        val unsynced = payeeDao.getUnsyncedPayees(userId)
        for (payee in unsynced) {
            val payload = SupabasePayee(
                id = payee.id,
                name = payee.name,
                logo = payee.logo,
                userId = payee.userId,
                priority = payee.priority
            )
            try {
                supabaseClient.from("payees").upsert(payload)
                payeeDao.updateSyncStatus(payee.id, 0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun pushLocalGroups(userId: String) {
        val unsynced = groupDao.getUnsyncedGroups(userId)
        for (group in unsynced) {
            val payload = SupabaseGroup(
                id = group.id,
                name = group.name,
                description = group.description,
                userId = group.userId,
                priority = group.priority
            )
            try {
                supabaseClient.from("transaction_groups").upsert(payload)
                groupDao.updateSyncStatus(group.id, 0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun pushLocalBudgets(userId: String) {
        val unsynced = budgetDao.getUnsyncedBudgets(userId)
        for (b in unsynced) {
            if (b.deleted == 1) {
                try {
                    supabaseClient.from("budgets").delete {
                        filter { eq("id", b.id) }
                    }
                    budgetDao.delete(b.id)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                val catList = try {
                    Json.decodeFromString<List<String>>(b.categories)
                } catch (e: Exception) {
                    emptyList()
                }
                val payload = SupabaseBudget(
                    id = b.id,
                    name = b.name,
                    logo = b.logo,
                    amount = b.amount,
                    interval = if (b.interval == "Monthly") "Month" else b.interval,
                    startDate = b.startDate,
                    categories = catList,
                    userId = b.userId
                )
                try {
                    supabaseClient.from("budgets").upsert(payload)
                    budgetDao.updateSyncStatus(b.id, 0)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private suspend fun pushLocalGoals(userId: String) {
        val unsynced = goalDao.getUnsyncedGoals(userId)
        for (g in unsynced) {
            if (g.deleted == 1) {
                try {
                    supabaseClient.from("goals").delete {
                        filter { eq("id", g.id) }
                    }
                    goalDao.delete(g.id)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                val payload = SupabaseGoal(
                    id = g.id,
                    name = g.name,
                    logo = g.logo,
                    goalAmount = g.goalAmount,
                    currentAmount = g.currentAmount,
                    userId = g.userId
                )
                try {
                    supabaseClient.from("goals").upsert(payload)
                    goalDao.updateSyncStatus(g.id, 0)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private suspend fun pushLocalQuickTransactions(userId: String) {
        val unsynced = quickTransactionDao.getUnsynced(userId)
        for (qt in unsynced) {
            if (qt.deleted == 1) {
                try {
                    supabaseClient.from("quick_transactions").delete {
                        filter { eq("id", qt.id) }
                    }
                    quickTransactionDao.delete(qt.id)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                val payload = SupabaseQuickTransaction(
                    id = qt.id,
                    name = qt.name,
                    type = qt.type,
                    amount = qt.amount,
                    categoryId = qt.categoryId,
                    payeeId = qt.payeeId,
                    description = qt.description,
                    userId = qt.userId,
                    productLink = qt.productLink,
                    priority = qt.priority,
                    identifier = qt.identifier
                )
                try {
                    supabaseClient.from("quick_transactions").upsert(payload)
                    quickTransactionDao.updateSyncStatus(qt.id, 0)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private suspend fun syncTransactions(userId: String, isPartial: Boolean) {
        var lastTid = 0
        if (isPartial) {
            lastTid = transactionDao.getMaxTid(userId) ?: 0
        } else {
            transactionDao.deleteAllForUser(userId)
        }

        val chunkSize = 1000
        var offset = 0
        var hasMore = true

        while (hasMore) {
            val response = supabaseClient.from("transactions").select(
                columns = Columns.raw("""
                    id, amount, description, transaction_timestamp, category_id, payee_id, group_id, type, user_id, product_link, tid, latitude, longitude, created_at, updated_at,
                    categories:category_id (name, icon, app_icon),
                    payees:payee_id (name, logo),
                    transaction_groups:group_id (name)
                """.trimIndent())
            ) {
                filter {
                    eq("user_id", userId)
                    gt("tid", lastTid)
                }
                order("tid", Order.ASCENDING)
                range(offset.toLong(), (offset + chunkSize - 1).toLong())
            }

            val remoteTxList = response.decodeList<SupabaseTransaction>()
            if (remoteTxList.isEmpty()) {
                hasMore = false
            } else {
                val entities = remoteTxList.map { item ->
                    TransactionEntity(
                        id = item.id,
                        amount = item.amount,
                        description = item.description,
                        transactionTimestamp = item.transactionTimestamp,
                        date = getTransactionDate(item.transactionTimestamp),
                        categoryId = item.categoryId,
                        categoryName = item.categories?.name,
                        categoryIcon = item.categories?.icon,
                        categoryAppIcon = item.categories?.appIcon,
                        payeeId = item.payeeId,
                        payeeName = item.payees?.name,
                        payeeLogo = item.payees?.logo,
                        type = item.type,
                        userId = item.userId,
                        productLink = item.productLink,
                        tid = item.tid,
                        latitude = item.latitude,
                        longitude = item.longitude,
                        groupId = item.groupId,
                        groupName = item.transactionGroups?.name,
                        syncStatus = 0,
                        createdAt = item.createdAt ?: Instant.now().toString(),
                        updatedAt = item.updatedAt ?: Instant.now().toString()
                    )
                }
                db.withTransaction {
                    transactionDao.insertOrReplace(entities)
                }
                offset += chunkSize
            }
        }
    }

    private suspend fun syncCategories(userId: String) {
        val response = supabaseClient.from("categories").select {
            filter { eq("user_id", userId) }
        }
        val remoteList = response.decodeList<SupabaseCategory>()
        val entities = remoteList.map { item ->
            CategoryEntity(
                id = item.id,
                name = item.name,
                type = item.type,
                icon = item.icon,
                appIcon = item.appIcon,
                userId = item.userId,
                isLivingCost = item.isLivingCost,
                syncStatus = 0,
                priority = item.priority
            )
        }
        db.withTransaction {
            categoryDao.deleteAllForUser(userId)
            categoryDao.insertOrReplace(entities)
        }
    }

    private suspend fun syncPayees(userId: String) {
        val response = supabaseClient.from("payees").select {
            filter { eq("user_id", userId) }
        }
        val remoteList = response.decodeList<SupabasePayee>()
        val entities = remoteList.map { item ->
            PayeeEntity(
                id = item.id,
                name = item.name,
                logo = item.logo,
                userId = item.userId,
                syncStatus = 0,
                priority = item.priority
            )
        }
        db.withTransaction {
            payeeDao.deleteAllForUser(userId)
            payeeDao.insertOrReplace(entities)
        }
    }

    private suspend fun syncGroups(userId: String) {
        val response = supabaseClient.from("transaction_groups").select {
            filter { eq("user_id", userId) }
        }
        val remoteList = response.decodeList<SupabaseGroup>()
        val entities = remoteList.map { item ->
            TransactionGroupEntity(
                id = item.id,
                name = item.name,
                description = item.description,
                userId = item.userId,
                priority = item.priority,
                syncStatus = 0
            )
        }
        db.withTransaction {
            groupDao.deleteAllForUser(userId)
            groupDao.insertOrReplace(entities)
        }
    }

    private suspend fun syncBudgets(userId: String) {
        val response = supabaseClient.from("budgets").select {
            filter { eq("user_id", userId) }
        }
        val remoteList = response.decodeList<SupabaseBudget>()
        val entities = remoteList.map { item ->
            val catStr = try {
                Json.encodeToString<List<String>>(item.categories)
            } catch (e: Exception) {
                "[]"
            }
            BudgetEntity(
                id = item.id,
                name = item.name,
                logo = item.logo,
                amount = item.amount,
                interval = if (item.interval == "Month") "Monthly" else item.interval,
                startDate = item.startDate,
                categories = catStr,
                userId = item.userId,
                syncStatus = 0,
                deleted = 0
            )
        }
        db.withTransaction {
            budgetDao.deleteAllForUser(userId)
            budgetDao.insertOrReplace(entities)
        }
    }

    private suspend fun syncGoals(userId: String) {
        val response = supabaseClient.from("goals").select {
            filter { eq("user_id", userId) }
        }
        val remoteList = response.decodeList<SupabaseGoal>()
        val entities = remoteList.map { item ->
            GoalEntity(
                id = item.id,
                name = item.name,
                logo = item.logo,
                goalAmount = item.goalAmount,
                currentAmount = item.currentAmount,
                userId = item.userId,
                syncStatus = 0,
                deleted = 0
            )
        }
        db.withTransaction {
            goalDao.deleteAllForUser(userId)
            goalDao.insertOrReplace(entities)
        }
    }

    private suspend fun syncQuickTransactions(userId: String) {
        val response = supabaseClient.from("quick_transactions").select {
            filter { eq("user_id", userId) }
        }
        val remoteList = response.decodeList<SupabaseQuickTransaction>()
        val entities = remoteList.map { item ->
            QuickTransactionEntity(
                id = item.id,
                name = item.name,
                type = item.type,
                amount = item.amount,
                categoryId = item.categoryId,
                payeeId = item.payeeId,
                description = item.description,
                userId = item.userId,
                productLink = item.productLink,
                priority = item.priority,
                identifier = item.identifier,
                syncStatus = 0,
                deleted = 0
            )
        }
        db.withTransaction {
            quickTransactionDao.deleteAllForUser(userId)
            quickTransactionDao.insertOrReplace(entities)
        }
    }

    private fun toSupabaseTransactionTimestamp(timestamp: String): String {
        return try {
            val instant = Instant.parse(timestamp)
            DateTimeFormatter.ISO_INSTANT.format(instant)
        } catch (e: Exception) {
            timestamp.replace(" ", "T")
        }
    }

    private fun getTransactionDate(timestamp: String): String {
        val pattern = Regex("^(\\d{4}-\\d{2}-\\d{2})[T ]")
        val matchResult = pattern.find(timestamp)
        if (matchResult != null) {
            return matchResult.groupValues[1]
        }
        return try {
            val instant = Instant.parse(timestamp)
            val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
            localDate.toString()
        } catch (e: Exception) {
            timestamp.substringBefore("T").substringBefore(" ")
        }
    }
}
