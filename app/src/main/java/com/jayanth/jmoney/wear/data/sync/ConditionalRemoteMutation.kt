package com.jayanth.jmoney.wear.data.sync

/**
 * Performs the remote mutation before asking the DAO to acknowledge the exact
 * uploaded snapshot. The acknowledgement callback must use a Room @Transaction
 * compare-and-update/delete method so an edit cannot slip between comparison
 * and acknowledgement.
 */
internal suspend fun <R, T> performRemoteMutationIfUnchanged(
    snapshot: T,
    remoteMutation: suspend () -> R,
    acknowledgeLocalIfUnchanged: suspend (T, R) -> Unit
): R {
    val result = remoteMutation()
    acknowledgeLocalIfUnchanged(snapshot, result)
    return result
}
