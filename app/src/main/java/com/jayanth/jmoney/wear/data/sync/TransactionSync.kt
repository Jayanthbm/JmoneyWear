package com.jayanth.jmoney.wear.data.sync

import androidx.room.withTransaction
import com.jayanth.jmoney.wear.data.database.AppDatabase
import com.jayanth.jmoney.wear.data.database.TransactionDao
import com.jayanth.jmoney.wear.data.database.TransactionEntity
import com.jayanth.jmoney.wear.data.network.dto.SupabaseTransaction
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class TidResponse(val tid: Int)

@Singleton
class TransactionSync @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val db: AppDatabase,
    private val transactionDao: TransactionDao
) {
    suspend fun pushLocalChanges(userId: String) {
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

    suspend fun syncRemoteChanges(userId: String, isPartial: Boolean) {
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

    private fun toSupabaseTransactionTimestamp(timestamp: String): String {
        return try {
            val instant = Instant.parse(timestamp)
            java.time.format.DateTimeFormatter.ISO_INSTANT.format(instant)
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
