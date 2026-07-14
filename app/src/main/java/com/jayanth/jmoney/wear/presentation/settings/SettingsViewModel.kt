package com.jayanth.jmoney.wear.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jayanth.jmoney.wear.data.repository.AuthRepository
import com.jayanth.jmoney.wear.data.repository.SyncRepository
import com.jayanth.jmoney.wear.data.repository.SyncState
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val syncRepository: SyncRepository
) : ViewModel() {

    val settingsState: StateFlow<SettingsState> = combine(
        syncRepository.syncState
    ) { (syncState) ->
        SettingsState(
            userEmail = authRepository.getCurrentUserEmail(),
            lastSyncTimestamp = syncRepository.getLastSyncTimestamp(),
            isSyncing = syncState is SyncState.Syncing,
            syncMessage = (syncState as? SyncState.Syncing)?.stage
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
            syncRepository.runFullSync()
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
