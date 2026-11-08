package com.jayanth.jmoney.wear.data.sync

/**
 * Acknowledges an uploaded snapshot only if the local row is still byte-for-byte
 * equal to the snapshot that was sent. An edit made while the network request is
 * in flight therefore remains pending for the next sync.
 */
internal suspend fun <R, T> performRemoteMutationIfUnchanged(
    snapshot: T,
    remoteMutation: suspend () -> R,
    readCurrent: suspend () -> T?,
    acknowledgeLocal: suspend (R) -> Unit
): R {
    val result = remoteMutation()
    if (readCurrent() == snapshot) {
        acknowledgeLocal(result)
    }
    return result
}
