package com.jayanth.jmoney.wear.data.sync

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.IOException

class ConditionalRemoteMutationTest {

    private data class FakeRow(val id: String, val value: String, val syncStatus: Int)

    @Test
    fun editMadeWhileUploadIsInFlightRemainsPending() = runBlocking {
        val uploadedSnapshot = FakeRow("row-1", "old value", 1)
        var currentRow: FakeRow? = uploadedSnapshot
        var acknowledgements = 0

        performRemoteMutationIfUnchanged(
            snapshot = uploadedSnapshot,
            remoteMutation = {
                // Simulate a user edit arriving after the upload payload was captured.
                currentRow = FakeRow("row-1", "new value", 1)
            },
            readCurrent = { currentRow },
            acknowledgeLocal = {
                acknowledgements++
                currentRow = currentRow?.copy(syncStatus = 0)
            }
        )

        assertEquals(FakeRow("row-1", "new value", 1), currentRow)
        assertEquals(0, acknowledgements)
    }

    @Test
    fun unchangedRowIsAcknowledgedAfterSuccessfulUpload() = runBlocking {
        val uploadedSnapshot = FakeRow("row-1", "same value", 1)
        var currentRow: FakeRow? = uploadedSnapshot
        var acknowledgements = 0

        performRemoteMutationIfUnchanged(
            snapshot = uploadedSnapshot,
            remoteMutation = { "server-ok" },
            readCurrent = { currentRow },
            acknowledgeLocal = { result ->
                assertEquals("server-ok", result)
                acknowledgements++
                currentRow = currentRow?.copy(syncStatus = 0)
            }
        )

        assertEquals(FakeRow("row-1", "same value", 0), currentRow)
        assertEquals(1, acknowledgements)
    }

    @Test
    fun rowDeletedLocallyWhileUploadIsInFlightIsNotRecreatedOrAcknowledged() = runBlocking {
        val uploadedSnapshot = FakeRow("row-1", "value", 1)
        var currentRow: FakeRow? = uploadedSnapshot
        var acknowledgements = 0

        performRemoteMutationIfUnchanged(
            snapshot = uploadedSnapshot,
            remoteMutation = { currentRow = null },
            readCurrent = { currentRow },
            acknowledgeLocal = { acknowledgements++ }
        )

        assertNull(currentRow)
        assertEquals(0, acknowledgements)
    }

    @Test
    fun failedRemoteMutationDoesNotReadOrAcknowledgeLocalRow() = runBlocking {
        val uploadedSnapshot = FakeRow("row-1", "value", 1)
        var reads = 0
        var acknowledgements = 0
        val failure = IOException("offline")

        try {
            performRemoteMutationIfUnchanged(
                snapshot = uploadedSnapshot,
                remoteMutation = { throw failure },
                readCurrent = { reads++; uploadedSnapshot },
                acknowledgeLocal = { acknowledgements++ }
            )
            throw AssertionError("Expected remote failure")
        } catch (actual: IOException) {
            assertEquals(failure, actual)
        }

        assertEquals(0, reads)
        assertEquals(0, acknowledgements)
    }
}
