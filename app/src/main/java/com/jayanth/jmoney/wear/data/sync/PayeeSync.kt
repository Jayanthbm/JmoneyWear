package com.jayanth.jmoney.wear.data.sync

import androidx.room.withTransaction
import com.jayanth.jmoney.wear.data.database.AppDatabase
import com.jayanth.jmoney.wear.data.database.PayeeDao
import com.jayanth.jmoney.wear.data.database.PayeeEntity
import com.jayanth.jmoney.wear.data.network.dto.SupabasePayee
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PayeeSync @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val db: AppDatabase,
    private val payeeDao: PayeeDao
) {
    suspend fun pushLocalChanges(userId: String) {
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
                    throw e
                }
        }
    }

    suspend fun syncRemoteChanges(userId: String) {
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
        if (remoteList.isEmpty() && payeeDao.getPayees(userId).isNotEmpty()) {
            throw IllegalStateException("Remote snapshot for payee is empty while local records exist; refusing destructive replacement")
        }

        db.withTransaction {
            val pendingLocalChanges = payeeDao.getUnsyncedPayees(userId)
            payeeDao.deleteAllForUser(userId)
            payeeDao.insertOrReplace(entities + pendingLocalChanges)
        }
    }
}
