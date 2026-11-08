package com.jayanth.jmoney.wear.data.sync

/**
 * Executes the ordered sync phases. A failure or cancellation stops the pipeline
 * immediately, so later remote snapshots cannot mask an incomplete upload.
 */
internal suspend fun executeSyncPipeline(
    onStage: (String) -> Unit,
    pushLocalChanges: suspend () -> Unit,
    syncTransactions: suspend () -> Unit,
    syncCategories: suspend () -> Unit,
    syncPayees: suspend () -> Unit,
    syncTransactionGroups: suspend () -> Unit,
    syncBudgets: suspend () -> Unit,
    syncGoals: suspend () -> Unit,
    syncQuickTransactions: suspend () -> Unit
) {
    onStage("Pushing local changes...")
    pushLocalChanges()

    onStage("Syncing Transactions...")
    syncTransactions()

    onStage("Syncing Categories...")
    syncCategories()

    onStage("Syncing Payees...")
    syncPayees()

    onStage("Syncing Transaction Groups...")
    syncTransactionGroups()

    onStage("Syncing Budgets...")
    syncBudgets()

    onStage("Syncing Goals...")
    syncGoals()

    onStage("Syncing Quick Transactions...")
    syncQuickTransactions()
}
