package com.jayanth.jmoney.wear.data.sync

import androidx.room.withTransaction
import com.jayanth.jmoney.wear.data.database.AppDatabase
import com.jayanth.jmoney.wear.data.database.BudgetDao
import com.jayanth.jmoney.wear.data.database.BudgetEntity
import com.jayanth.jmoney.wear.data.network.dto.SupabaseBudget
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetSync @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val db: AppDatabase,
    private val budgetDao: BudgetDao
) {
    suspend fun pushLocalChanges(userId: String) {
        val unsynced = budgetDao.getUnsyncedBudgets(userId)
        for (b in unsynced) {
            if (b.deleted == 1) {
                performRemoteMutation(
                    remoteMutation = {
                        supabaseClient.from("budgets").delete {
                            filter {
                                eq("id", b.id)
                                eq("user_id", userId)
                            }
                        }
                    },
                    acknowledgeLocal = { budgetDao.delete(b.id) }
                )
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
                performRemoteMutation(
                    remoteMutation = { supabaseClient.from("budgets").upsert(payload) },
                    acknowledgeLocal = { budgetDao.updateSyncStatus(b.id, 0) }
                )
            }
        }
    }

    suspend fun syncRemoteChanges(userId: String) {
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
        if (remoteList.isEmpty() && budgetDao.getBudgets(userId).isNotEmpty()) {
            throw IllegalStateException("Remote snapshot for budget is empty while local records exist; refusing destructive replacement")
        }

        db.withTransaction {
            val pendingLocalChanges = budgetDao.getUnsyncedBudgets(userId)
            budgetDao.deleteAllForUser(userId)
            budgetDao.insertOrReplace(entities + pendingLocalChanges)
        }
    }
}
