package com.jayanth.jmoney.wear.data.sync

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

class RemoteMutationTest {

    @Test
    fun failedRemoteUploadLeavesLocalChangeUnacknowledged() {
        val uploadFailure = IOException("Remote upsert failed")
        var localAcknowledgements = 0

        val thrown = assertThrows(IOException::class.java) {
            runBlocking {
                performRemoteMutation(
                    remoteMutation = { throw uploadFailure },
                    acknowledgeLocal = { localAcknowledgements++ }
                )
            }
        }

        assertSame(uploadFailure, thrown)
        assertEquals(0, localAcknowledgements)
    }

    @Test
    fun successfulRemoteUploadAcknowledgesLocalChangeExactlyOnce() = runBlocking {
        var localAcknowledgements = 0
        val result = performRemoteMutation(
            remoteMutation = { "server-confirmed" },
            acknowledgeLocal = { value ->
                assertEquals("server-confirmed", value)
                localAcknowledgements++
            }
        )

        assertEquals("server-confirmed", result)
        assertEquals(1, localAcknowledgements)
    }

    @Test
    fun localAcknowledgementFailureIsPropagated() {
        val acknowledgementFailure = IOException("Room update failed")

        val thrown = assertThrows(IOException::class.java) {
            runBlocking {
                performRemoteMutation(
                    remoteMutation = { Unit },
                    acknowledgeLocal = { throw acknowledgementFailure }
                )
            }
        }

        assertSame(acknowledgementFailure, thrown)
    }

    @Test
    fun cancellationDuringRemoteMutationDoesNotAcknowledgeLocalChange() {
        val cancellation = CancellationException("Upload cancelled")
        var localAcknowledgements = 0

        val thrown = assertThrows(CancellationException::class.java) {
            runBlocking {
                performRemoteMutation(
                    remoteMutation = { throw cancellation },
                    acknowledgeLocal = { localAcknowledgements++ }
                )
            }
        }

        assertSame(cancellation, thrown)
        assertEquals(0, localAcknowledgements)
    }
}
