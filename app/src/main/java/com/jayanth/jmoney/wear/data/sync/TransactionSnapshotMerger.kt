package com.jayanth.jmoney.wear.data.sync

import com.jayanth.jmoney.wear.data.database.TransactionEntity

/**
 * Combines a fetched remote snapshot with local changes that have not been
 * acknowledged by the server. Pending local rows are applied last so a local
 * edit or deletion tombstone wins over the matching remote row.
 */
internal fun mergeRemoteTransactionsWithPendingChanges(
    remoteTransactions: List<TransactionEntity>,
    pendingLocalChanges: List<TransactionEntity>
): List<TransactionEntity> {
    val merged = LinkedHashMap<String, TransactionEntity>()
    remoteTransactions.forEach { merged[it.id] = it }
    pendingLocalChanges.forEach { merged[it.id] = it }
    return merged.values.toList()
}
