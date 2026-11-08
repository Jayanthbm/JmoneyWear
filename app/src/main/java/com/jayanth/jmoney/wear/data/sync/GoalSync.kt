package com.jayanth.jmoney.wear.data.sync

import androidx.room.withTransaction
import com.jayanth.jmoney.wear.data.database.AppDatabase
import com.jayanth.jmoney.wear.data.database.GoalDao
import com.jayanth.jmoney.wear.data.database.GoalEntity
import com.jayanth.jmoney.wear.data.network.dto.SupabaseGoal
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalSync @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val db: AppDatabase,
    private val goalDao: GoalDao
) {
    suspend fun pushLocalChanges(userId: String) {
        val unsynced = goalDao.getUnsyncedGoals(userId)
        for (g in unsynced) {
            if (g.deleted == 1) {
                performRemoteMutationIfUnchanged(
                    snapshot = g,
                    readCurrent = { goalDao.getById(g.id) },

                    remoteMutation = {
                        supabaseClient.from("goals").delete {
                            filter {
                                eq("id", g.id)
                                eq("user_id", userId)
                            }
                        }
                    },
                    acknowledgeLocal = { goalDao.delete(g.id) }
                )
            } else {
                val payload = SupabaseGoal(
                    id = g.id,
                    name = g.name,
                    logo = g.logo,
                    goalAmount = g.goalAmount,
                    currentAmount = g.currentAmount,
                    userId = g.userId
                )
                performRemoteMutationIfUnchanged(
                    snapshot = g,
                    readCurrent = { goalDao.getById(g.id) },

                    remoteMutation = { supabaseClient.from("goals").upsert(payload) },
                    acknowledgeLocal = { goalDao.updateSyncStatus(g.id, 0) }
                )
            }
        }
    }

    suspend fun syncRemoteChanges(userId: String) {
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
        if (remoteList.isEmpty() && goalDao.getGoals(userId).isNotEmpty()) {
            throw IllegalStateException("Remote snapshot for goal is empty while local records exist; refusing destructive replacement")
        }

        db.withTransaction {
            val pendingLocalChanges = goalDao.getUnsyncedGoals(userId)
            goalDao.deleteAllForUser(userId)
            goalDao.insertOrReplace(entities + pendingLocalChanges)
        }
    }
}
