package com.jayanth.jmoney.wear.data.sync

/**
 * A local row is acknowledged only after its remote mutation succeeds.
 * Exceptions and coroutine cancellation deliberately propagate unchanged.
 */
internal suspend fun <T> performRemoteMutation(
    remoteMutation: suspend () -> T,
    acknowledgeLocal: suspend (T) -> Unit
): T {
    val result = remoteMutation()
    acknowledgeLocal(result)
    return result
}
