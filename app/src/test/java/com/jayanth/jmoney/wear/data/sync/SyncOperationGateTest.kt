package com.jayanth.jmoney.wear.data.sync

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncOperationGateTest {

    @Test
    fun concurrentSyncAndResetOperationsAreSerialized() = runBlocking {
        val gate = SyncOperationGate()
        val firstEntered = CompletableDeferred<Unit>()
        val releaseFirst = CompletableDeferred<Unit>()
        val secondEntered = CompletableDeferred<Unit>()
        var activeOperations = 0
        var maxConcurrentOperations = 0

        val first = launch {
            gate.withLock {
                activeOperations++
                maxConcurrentOperations = maxOf(maxConcurrentOperations, activeOperations)
                firstEntered.complete(Unit)
                releaseFirst.await()
                activeOperations--
            }
        }

        firstEntered.await()
        val second = async {
            gate.withLock {
                activeOperations++
                maxConcurrentOperations = maxOf(maxConcurrentOperations, activeOperations)
                secondEntered.complete(Unit)
                activeOperations--
            }
        }

        yield()
        assertFalse("Second operation must wait for the first", secondEntered.isCompleted)

        releaseFirst.complete(Unit)
        first.join()
        second.await()

        assertTrue(secondEntered.isCompleted)
        assertEquals(1, maxConcurrentOperations)
        assertEquals(0, activeOperations)
    }
}
