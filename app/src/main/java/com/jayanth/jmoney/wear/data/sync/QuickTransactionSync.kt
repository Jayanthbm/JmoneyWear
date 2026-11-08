package com.jayanth.jmoney.wear.data.sync

import androidx.room.withTransaction
import com.jayanth.jmoney.wear.data.database.AppDatabase
import com.jayanth.jmoney.wear.data.database.QuickTransactionDao
import com.jayanth.jmoney.wear.data.database.QuickTransactionEntity
import com.jayanth.jmoney.wear.data.network.dto.SupabaseQuickTransaction
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuickTransactionSync @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val db: AppDatabase,
    private val quickTransactionDao: QuickTransactionDao
) {
    suspend fun pushLocalChanges(userId: String) {
        val unsynced = quickTransactionDao.getUnsynced(userId)
        for (qt in unsynced) {
            if (qt.deleted == 1) {
                performRemoteMutationIfUnchanged(
                    snapshot = qt,

                    remoteMutation = {
                        supabaseClient.from("quick_transactions").delete {
                            filter {
                                eq("id", qt.id)
                                eq("user_id", userId)
                            }
                        }
                    },
                    acknowledgeLocalIfUnchanged = { uploadedSnapshot, _ -> quickTransactionDao.deleteIfUnchanged(uploadedSnapshot) }
                )
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
                performRemoteMutationIfUnchanged(
                    snapshot = qt,

                    remoteMutation = { supabaseClient.from("quick_transactions").upsert(payload) },
                    acknowledgeLocalIfUnchanged = { uploadedSnapshot, _ -> quickTransactionDao.acknowledgeIfUnchanged(uploadedSnapshot) }
                )
            }
        }
    }

    suspend fun syncRemoteChanges(userId: String) {
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
        if (remoteList.isEmpty() && quickTransactionDao.getQuickTransactions(userId).isNotEmpty()) {
            throw IllegalStateException("Remote snapshot for quickTransaction is empty while local records exist; refusing destructive replacement")
        }

        db.withTransaction {
            val pendingLocalChanges = quickTransactionDao.getUnsynced(userId)
            quickTransactionDao.deleteAllForUser(userId)
            quickTransactionDao.insertOrReplace(entities + pendingLocalChanges)
        }
    }
}
