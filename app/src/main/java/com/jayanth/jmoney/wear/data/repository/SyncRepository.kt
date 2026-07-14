package com.jayanth.jmoney.wear.data.repository

import kotlinx.coroutines.flow.StateFlow

interface SyncRepository {
    val syncState: StateFlow<SyncState>
    
    suspend fun runFullSync(): Result<Unit>
    suspend fun runPartialSync(): Result<Unit>
    suspend fun resetLocalData(): Result<Unit>
    suspend fun hasSyncedBefore(): Boolean
    fun getLastSyncTimestamp(): Long
}

sealed interface SyncState {
    object Idle : SyncState
    data class Syncing(val stage: String) : SyncState
    object Success : SyncState
    data class Error(val message: String) : SyncState
}
