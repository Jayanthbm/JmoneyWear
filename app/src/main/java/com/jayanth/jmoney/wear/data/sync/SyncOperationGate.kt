package com.jayanth.jmoney.wear.data.sync

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Serializes full/partial sync and local reset operations so they cannot
 * overwrite one another's snapshots concurrently.
 */
internal class SyncOperationGate {
    private val mutex = Mutex()

    suspend fun <T> withLock(operation: suspend () -> T): T =
        mutex.withLock { operation() }
}
