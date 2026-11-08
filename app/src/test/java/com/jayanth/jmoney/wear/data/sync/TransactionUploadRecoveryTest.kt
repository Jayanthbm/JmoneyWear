package com.jayanth.jmoney.wear.data.sync

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

class TransactionUploadRecoveryTest {

    @Test
    fun successfulFirstResponseDoesNotRetry() = runBlocking {
        var retryCount = 0
        val tid = resolveTransactionUploadTid(
            upsertReturningTid = { 42 },
            retryUpsert = { retryCount++ },
            readServerTid = { error("Should not read after a successful response") }
        )

        assertEquals(42, tid)
        assertEquals(0, retryCount)
    }

    @Test
    fun ambiguousFirstResponseRetriesAndReadsServerTid() = runBlocking {
        var retryCount = 0
        var readCount = 0
        val tid = resolveTransactionUploadTid(
            upsertReturningTid = { throw IOException("Response decoding failed") },
            retryUpsert = { retryCount++ },
            readServerTid = {
                readCount++
                91
            }
        )

        assertEquals(91, tid)
        assertEquals(1, retryCount)
        assertEquals(1, readCount)
    }

    @Test
    fun firstAttemptCancellationDoesNotRetry() {
        var retryCount = 0
        assertThrows(CancellationException::class.java) {
            runBlocking {
                resolveTransactionUploadTid(
                    upsertReturningTid = { throw CancellationException("Cancelled") },
                    retryUpsert = { retryCount++ },
                    readServerTid = { 10 }
                )
            }
        }
        assertEquals(0, retryCount)
    }

    @Test
    fun retryFailureRetainsOriginalFailureAsSuppressed() {
        val firstFailure = IOException("Initial response failed")
        val retryFailure = IOException("Retry write failed")
        val thrown = assertThrows(IOException::class.java) {
            runBlocking {
                resolveTransactionUploadTid(
                    upsertReturningTid = { throw firstFailure },
                    retryUpsert = { throw retryFailure },
                    readServerTid = { 10 }
                )
            }
        }

        assertSame(retryFailure, thrown)
        assertEquals(1, thrown.suppressed.size)
        assertSame(firstFailure, thrown.suppressed[0])
    }
}
