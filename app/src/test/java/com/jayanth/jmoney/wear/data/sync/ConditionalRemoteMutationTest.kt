package com.jayanth.jmoney.wear.data.sync

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.IOException

class ConditionalRemoteMutationTest {

    private data class FakeRow(val id: String, val value: String, val syncStatus: Int)

    /**
     * In-memory stand-in for the DAO's @Transaction compare-and-update/delete
     * methods. The comparison and acknowledgement happen in one method call.
     */
    private class FakeDao(var row: FakeRow?) {
        var acknowledgements = 0

        suspend fun acknowledgeIfUnchanged(snapshot: FakeRow) {
            if (row == snapshot) {
                acknowledgements++
                row = row?.copy(syncStatus = 0)
            }
        }

        suspend fun deleteIfUnchanged(snapshot: FakeRow) {
            if (row == snapshot) {
                acknowledgements++
                row = null
            }
        }
    }

    @Test
    fun editMadeWhileUploadIsInFlightRemainsPending() = runBlocking {
        val uploadedSnapshot = FakeRow("row-1", "old value", 1)
        val dao = FakeDao(uploadedSnapshot)

        performRemoteMutationIfUnchanged(
            snapshot = uploadedSnapshot,
            remoteMutation = {
                // A local edit arrives after payload capture but before acknowledgement.
                dao.row = FakeRow("row-1", "new value", 1)
            },
            acknowledgeLocalIfUnchanged = { snapshot, _ ->
                dao.acknowledgeIfUnchanged(snapshot)
            }
        )

        assertEquals(FakeRow("row-1", "new value", 1), dao.row)
        assertEquals(0, dao.acknowledgements)
    }

    @Test
    fun unchangedRowIsAcknowledgedAfterSuccessfulUpload() = runBlocking {
        val uploadedSnapshot = FakeRow("row-1", "same value", 1)
        val dao = FakeDao(uploadedSnapshot)

        performRemoteMutationIfUnchanged(
            snapshot = uploadedSnapshot,
            remoteMutation = { "server-ok" },
            acknowledgeLocalIfUnchanged = { snapshot, result ->
                assertEquals("server-ok", result)
                dao.acknowledgeIfUnchanged(snapshot)
            }
        )

        assertEquals(FakeRow("row-1", "same value", 0), dao.row)
        assertEquals(1, dao.acknowledgements)
    }

    @Test
    fun locallyRemovedRowIsNotRecreatedOrAcknowledged() = runBlocking {
        val uploadedSnapshot = FakeRow("row-1", "value", 1)
        val dao = FakeDao(uploadedSnapshot)

        performRemoteMutationIfUnchanged(
            snapshot = uploadedSnapshot,
            remoteMutation = { dao.row = null },
            acknowledgeLocalIfUnchanged = { snapshot, _ ->
                dao.acknowledgeIfUnchanged(snapshot)
            }
        )

        assertNull(dao.row)
        assertEquals(0, dao.acknowledgements)
    }

    @Test
    fun remoteDeleteDoesNotDeleteAReplacementEdit() = runBlocking {
        val uploadedSnapshot = FakeRow("row-1", "old value", 1)
        val dao = FakeDao(uploadedSnapshot)

        performRemoteMutationIfUnchanged(
            snapshot = uploadedSnapshot,
            remoteMutation = {
                // The user edits/recreates the row while the remote delete is in flight.
                dao.row = FakeRow("row-1", "replacement value", 1)
            },
            acknowledgeLocalIfUnchanged = { snapshot, _ ->
                dao.deleteIfUnchanged(snapshot)
            }
        )

        assertEquals(FakeRow("row-1", "replacement value", 1), dao.row)
        assertEquals(0, dao.acknowledgements)
    }

    @Test
    fun failedRemoteMutationDoesNotAcknowledgeLocalRow() = runBlocking {
        val uploadedSnapshot = FakeRow("row-1", "value", 1)
        val dao = FakeDao(uploadedSnapshot)
        val failure = IOException("offline")

        try {
            performRemoteMutationIfUnchanged(
                snapshot = uploadedSnapshot,
                remoteMutation = { throw failure },
                acknowledgeLocalIfUnchanged = { snapshot, _ ->
                    dao.acknowledgeIfUnchanged(snapshot)
                }
            )
            throw AssertionError("Expected remote failure")
        } catch (actual: IOException) {
            assertEquals(failure, actual)
        }

        assertEquals(uploadedSnapshot, dao.row)
        assertEquals(0, dao.acknowledgements)
    }
}
