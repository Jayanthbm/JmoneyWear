package com.jayanth.jmoney.wear.data.sync

import androidx.room.withTransaction
import com.jayanth.jmoney.wear.data.database.AppDatabase
import com.jayanth.jmoney.wear.data.database.TransactionGroupDao
import com.jayanth.jmoney.wear.data.database.TransactionGroupEntity
import com.jayanth.jmoney.wear.data.network.dto.SupabaseGroup
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionGroupSync @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val db: AppDatabase,
    private val groupDao: TransactionGroupDao
) {
    suspend fun pushLocalChanges(userId: String) {
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

    suspend fun syncRemoteChanges(userId: String) {
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
}
