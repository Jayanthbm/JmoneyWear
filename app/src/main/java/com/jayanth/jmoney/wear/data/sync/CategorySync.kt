package com.jayanth.jmoney.wear.data.sync

import androidx.room.withTransaction
import com.jayanth.jmoney.wear.data.database.AppDatabase
import com.jayanth.jmoney.wear.data.database.CategoryDao
import com.jayanth.jmoney.wear.data.database.CategoryEntity
import com.jayanth.jmoney.wear.data.network.dto.SupabaseCategory
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategorySync @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val db: AppDatabase,
    private val categoryDao: CategoryDao
) {
    suspend fun pushLocalChanges(userId: String) {
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

    suspend fun syncRemoteChanges(userId: String) {
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
}
