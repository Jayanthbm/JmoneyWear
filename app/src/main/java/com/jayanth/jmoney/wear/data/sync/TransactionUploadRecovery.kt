package com.jayanth.jmoney.wear.data.sync

import kotlinx.coroutines.CancellationException

/**
 * Recovers an ambiguous transaction upsert without losing the server cursor.
 *
 * The first write may have reached the server even if decoding its returned row
 * failed. In that case the caller retries the idempotent upsert and explicitly
 * reads the server-assigned tid before it can mark the local row clean.
 */
internal suspend fun resolveTransactionUploadTid(
    upsertReturningTid: suspend () -> Int,
    retryUpsert: suspend () -> Unit,
    readServerTid: suspend () -> Int
): Int {
    return try {
        upsertReturningTid()
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (firstFailure: Exception) {
        try {
            retryUpsert()
            readServerTid()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (retryFailure: Exception) {
            retryFailure.addSuppressed(firstFailure)
            throw retryFailure
        }
    }
}
