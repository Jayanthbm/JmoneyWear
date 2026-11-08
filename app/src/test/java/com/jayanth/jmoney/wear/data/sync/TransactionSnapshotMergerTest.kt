package com.jayanth.jmoney.wear.data.sync

import com.jayanth.jmoney.wear.data.database.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionSnapshotMergerTest {

    @Test
    fun remoteRowsAreRetainedWhenThereAreNoPendingChanges() {
        val remote = listOf(transaction(id = "remote-1"), transaction(id = "remote-2"))

        val result = mergeRemoteTransactionsWithPendingChanges(remote, emptyList())

        assertEquals(remote, result)
    }

    @Test
    fun pendingLocalEditWinsOverMatchingRemoteRow() {
        val remote = listOf(
            transaction(id = "shared", description = "Server value", amount = 10.0),
            transaction(id = "remote-only")
        )
        val pending = listOf(
            transaction(id = "shared", description = "Local edit", amount = 25.0, syncStatus = 1)
        )

        val result = mergeRemoteTransactionsWithPendingChanges(remote, pending)

        assertEquals(2, result.size)
        assertEquals("Local edit", result.first { it.id == "shared" }.description)
        assertEquals(25.0, result.first { it.id == "shared" }.amount, 0.0)
        assertEquals(1, result.first { it.id == "shared" }.syncStatus)
        assertTrue(result.any { it.id == "remote-only" })
    }

    @Test
    fun pendingDeletionTombstoneWinsOverRemoteRow() {
        val remote = listOf(transaction(id = "to-delete", description = "Still on server"))
        val tombstone = transaction(id = "to-delete", description = "Local deletion", deleted = 1, syncStatus = 1)

        val result = mergeRemoteTransactionsWithPendingChanges(remote, listOf(tombstone))

        assertEquals(1, result.size)
        assertEquals(1, result.single().deleted)
        assertEquals("Local deletion", result.single().description)
    }

    private fun transaction(
        id: String,
        description: String? = "Test transaction",
        amount: Double = 10.0,
        syncStatus: Int = 0,
        deleted: Int = 0
    ) = TransactionEntity(
        id = id,
        amount = amount,
        description = description,
        transactionTimestamp = "2026-11-08T10:00:00",
        date = "2026-11-08",
        categoryId = null,
        categoryName = null,
        categoryIcon = null,
        categoryAppIcon = null,
        payeeId = null,
        payeeName = null,
        payeeLogo = null,
        type = "expense",
        userId = "test-user",
        productLink = null,
        tid = 1,
        latitude = null,
        longitude = null,
        groupId = null,
        groupName = null,
        syncStatus = syncStatus,
        createdAt = "2026-11-08T10:00:00Z",
        updatedAt = "2026-11-08T10:00:00Z",
        deleted = deleted
    )
}
