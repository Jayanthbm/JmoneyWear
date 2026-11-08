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
                supabaseClient.from("transactions").delete {
                    filter {
                        eq("id", tx.id)
                        eq("user_id", userId)
                    }
                }
                transactionDao.delete(tx.id)
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
                val uploadedTid = resolveTransactionUploadTid(
                    upsertReturningTid = {
                        supabaseClient.from("transactions").upsert(payload) {
                            select(Columns.list("tid"))
                        }.decodeSingle<TidResponse>().tid
                    },
                    retryUpsert = {
                        supabaseClient.from("transactions").upsert(payload)
                    },
                    readServerTid = {
                        supabaseClient.from("transactions").select(
                            columns = Columns.list("tid")
                        ) {
                            filter {
                                eq("id", tx.id)
                                eq("user_id", userId)
                            }
                        }.decodeSingle<TidResponse>().tid
                    }
                )
                transactionDao.updateTidAndSyncStatus(tx.id, uploadedTid)
            }
        }
    }

    suspend fun syncRemoteChanges(userId: String, isPartial: Boolean) {
        var lastTid = 0
        if (isPartial) {
            lastTid = transactionDao.getMaxTid(userId) ?: 0
        }

        val remoteEntities = fetchCompleteRemoteSnapshot(pageSize = 1000) { offset, pageSize ->
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
                range(offset.toLong(), (offset + pageSize - 1).toLong())
            }

            response.decodeList<SupabaseTransaction>().map { item ->
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
        }

        if (!isPartial) {
            requireSafeRemoteSnapshot(
                remoteCount = remoteEntities.size,
                localCount = transactionDao.getTransactions(userId).size,
                entityName = "transaction"
            )
        }

        // Do not touch the local snapshot until every remote page has been fetched
        // and decoded successfully. Preserve pending local edits and tombstones.
        db.withTransaction {
            if (!isPartial) {
                val pendingLocalChanges = transactionDao.getUnsyncedTransactions(userId)
                transactionDao.deleteAllForUser(userId)
                transactionDao.insertOrReplace(mergeRemoteTransactionsWithPendingChanges(remoteEntities, pendingLocalChanges))
            } else if (remoteEntities.isNotEmpty()) {
                transactionDao.insertOrReplace(remoteEntities)
            }
        }
    }

    private fun toSupabaseTransactionTimestamp(timestamp: String): String {
        return try {
            val hasTimezone = timestamp.endsWith("Z") || timestamp.contains("+") || (timestamp.substringAfter("T", "").contains("-"))
            val instant = if (hasTimezone) {
                Instant.parse(timestamp)
            } else {
                return timestamp.replace(" ", "T")
            }
            val localDateTime = java.time.LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").format(localDateTime)
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
