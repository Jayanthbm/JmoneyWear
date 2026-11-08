package com.jayanth.jmoney.wear.data.sync

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

class SyncPipelineTest {

    @Test
    fun failedLocalUploadStopsBeforeAnyRemoteSnapshotRefresh() {
        val uploadFailure = IOException("Category upload failed")
        val stages = mutableListOf<String>()
        val operations = mutableListOf<String>()

        val thrown = assertThrows(IOException::class.java) {
            runBlocking {
                executeSyncPipeline(
                    onStage = { stage -> stages.add(stage) },
                    pushLocalChanges = {
                        operations += "transaction-upload-succeeded"
                        operations += "category-upload"
                        throw uploadFailure
                    },
                    syncTransactions = { operations += "transactions-refresh" },
                    syncCategories = { operations += "categories-refresh" },
                    syncPayees = { operations += "payees-refresh" },
                    syncTransactionGroups = { operations += "groups-refresh" },
                    syncBudgets = { operations += "budgets-refresh" },
                    syncGoals = { operations += "goals-refresh" },
                    syncQuickTransactions = { operations += "quick-transactions-refresh" }
                )
            }
        }

        assertSame(uploadFailure, thrown)
        assertEquals(listOf("Pushing local changes..."), stages)
        assertEquals(
            listOf("transaction-upload-succeeded", "category-upload"),
            operations
        )
    }

    @Test
    fun failedRemoteSnapshotStopsLaterEntitiesFromRefreshing() {
        val snapshotFailure = IOException("Category snapshot failed")
        val stages = mutableListOf<String>()
        val operations = mutableListOf<String>()

        val thrown = assertThrows(IOException::class.java) {
            runBlocking {
                executeSyncPipeline(
                    onStage = { stage -> stages.add(stage) },
                    pushLocalChanges = { operations += "push" },
                    syncTransactions = { operations += "transactions" },
                    syncCategories = {
                        operations += "categories"
                        throw snapshotFailure
                    },
                    syncPayees = { operations += "payees" },
                    syncTransactionGroups = { operations += "groups" },
                    syncBudgets = { operations += "budgets" },
                    syncGoals = { operations += "goals" },
                    syncQuickTransactions = { operations += "quick-transactions" }
                )
            }
        }

        assertSame(snapshotFailure, thrown)
        assertEquals(
            listOf("Pushing local changes...", "Syncing Transactions...", "Syncing Categories..."),
            stages
        )
        assertEquals(listOf("push", "transactions", "categories"), operations)
    }

    @Test
    fun successfulPipelineRunsEveryPhaseInDependencyOrder() = runBlocking {
        val stages = mutableListOf<String>()
        val operations = mutableListOf<String>()

        executeSyncPipeline(
            onStage = { stage -> stages.add(stage) },
            pushLocalChanges = { operations += "push" },
            syncTransactions = { operations += "transactions" },
            syncCategories = { operations += "categories" },
            syncPayees = { operations += "payees" },
            syncTransactionGroups = { operations += "groups" },
            syncBudgets = { operations += "budgets" },
            syncGoals = { operations += "goals" },
            syncQuickTransactions = { operations += "quick-transactions" }
        )

        assertEquals(
            listOf("push", "transactions", "categories", "payees", "groups", "budgets", "goals", "quick-transactions"),
            operations
        )
        assertEquals(
            listOf(
                "Pushing local changes...",
                "Syncing Transactions...",
                "Syncing Categories...",
                "Syncing Payees...",
                "Syncing Transaction Groups...",
                "Syncing Budgets...",
                "Syncing Goals...",
                "Syncing Quick Transactions..."
            ),
            stages
        )
    }

    @Test
    fun cancellationDuringUploadPropagatesWithoutStartingRefreshes() {
        val cancellation = CancellationException("Sync cancelled")
        val operations = mutableListOf<String>()

        val thrown = assertThrows(CancellationException::class.java) {
            runBlocking {
                executeSyncPipeline(
                    onStage = {},
                    pushLocalChanges = { throw cancellation },
                    syncTransactions = { operations += "transactions" },
                    syncCategories = { operations += "categories" },
                    syncPayees = { operations += "payees" },
                    syncTransactionGroups = { operations += "groups" },
                    syncBudgets = { operations += "budgets" },
                    syncGoals = { operations += "goals" },
                    syncQuickTransactions = { operations += "quick-transactions" }
                )
            }
        }

        assertSame(cancellation, thrown)
        assertEquals(emptyList<String>(), operations)
    }
}
