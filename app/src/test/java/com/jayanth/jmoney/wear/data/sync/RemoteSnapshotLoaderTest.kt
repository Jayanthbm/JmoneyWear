package com.jayanth.jmoney.wear.data.sync

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

class RemoteSnapshotLoaderTest {

    @Test
    fun fetchesAllPagesInOrder() = runBlocking {
        val offsets = mutableListOf<Int>()
        val result = fetchCompleteRemoteSnapshot(pageSize = 2) { offset, size ->
            offsets += offset
            when (offset) {
                0 -> listOf(1, 2)
                2 -> listOf(3, 4)
                else -> emptyList()
            }.take(size)
        }

        assertEquals(listOf(1, 2, 3, 4), result)
        assertEquals(listOf(0, 2, 4), offsets)
    }

    @Test
    fun stopsAfterShortFinalPage() = runBlocking {
        var calls = 0
        val result = fetchCompleteRemoteSnapshot(pageSize = 3) { offset, _ ->
            calls++
            if (offset == 0) listOf("a", "b") else error("Unexpected extra page")
        }

        assertEquals(listOf("a", "b"), result)
        assertEquals(1, calls)
    }

    @Test
    fun pageFailureDoesNotReturnPartialSnapshot() {
        val failure = IOException("Second page unavailable")
        val thrown = assertThrows(IOException::class.java) {
            runBlocking {
                fetchCompleteRemoteSnapshot(pageSize = 2) { offset, _ ->
                    if (offset == 0) listOf(1, 2) else throw failure
                }
            }
        }
        assertEquals(failure, thrown)
    }

    @Test
    fun cancellationIsNotConvertedIntoPartialSnapshot() {
        assertThrows(CancellationException::class.java) {
            runBlocking {
                val fetchPage: suspend (Int, Int) -> List<Int> = { _, _ ->
                    throw CancellationException("Cancelled while paging")
                }
                fetchCompleteRemoteSnapshot(pageSize = 1, fetchPage = fetchPage)
            }
        }
    }

    @Test
    fun emptyRemoteSnapshotIsRejectedWhenLocalRowsExist() {
        assertThrows(IllegalStateException::class.java) {
            requireSafeRemoteSnapshot(remoteCount = 0, localCount = 5, entityName = "transaction")
        }
    }

    @Test
    fun emptyRemoteSnapshotIsAllowedWhenLocalDatasetIsAlsoEmpty() {
        requireSafeRemoteSnapshot(remoteCount = 0, localCount = 0, entityName = "transaction")
    }

    @Test
    fun nonEmptyRemoteSnapshotIsAllowed() {
        requireSafeRemoteSnapshot(remoteCount = 3, localCount = 5, entityName = "transaction")
    }
}
