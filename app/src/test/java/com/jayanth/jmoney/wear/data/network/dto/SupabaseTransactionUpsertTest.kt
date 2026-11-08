package com.jayanth.jmoney.wear.data.network.dto

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SupabaseTransactionUpsertTest {

    @Test
    fun writePayloadLeavesServerManagedTidOut() {
        val payload = SupabaseTransactionUpsert(
            id = "transaction-id",
            amount = 125.0,
            description = "Watch transaction",
            transactionTimestamp = "2026-11-08T10:00:00",
            categoryId = null,
            payeeId = null,
            groupId = null,
            type = "Expense",
            userId = "user-id"
        )

        val json = Json.encodeToString(payload)

        assertFalse("The client must not send a local placeholder tid", json.contains("\"tid\""))
        assertTrue(json.contains("\"transaction_timestamp\""))
        assertTrue(json.contains("\"user_id\""))
    }
}
