package com.jayanth.jmoney.wear.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jayanth.jmoney.wear.data.repository.AuthRepository
import com.jayanth.jmoney.wear.data.repository.SyncRepository
import com.jayanth.jmoney.wear.data.repository.SyncState
import com.jayanth.jmoney.wear.data.work.TileUpdateHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val userEmail: String? = null,
    val lastSyncTimestamp: Long = 0L,
    val isSyncing: Boolean = false,
    val syncMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    @Suppress("INAPPLICABLE_JVM_NAME")
    @ApplicationContext private val context: Context
) : ViewModel() {

    val settingsState: StateFlow<SettingsState> = combine(
        syncRepository.syncState
    ) { (syncState) ->
        SettingsState(
            userEmail = authRepository.getCurrentUserEmail(),
            lastSyncTimestamp = syncRepository.getLastSyncTimestamp(),
            isSyncing = syncState is SyncState.Syncing,
            syncMessage = when (syncState) {
                is SyncState.Syncing -> syncState.stage
                is SyncState.Error -> "Sync failed: ${syncState.message}"
                SyncState.Success -> "Sync completed"
                SyncState.Idle -> null
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsState(
            userEmail = authRepository.getCurrentUserEmail(),
            lastSyncTimestamp = syncRepository.getLastSyncTimestamp()
        )
    )

    fun forceFullSync() {
        viewModelScope.launch {
            val result = syncRepository.runFullSync()
            if (result.isSuccess) {
                TileUpdateHelper.requestAllTileUpdates(context)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
