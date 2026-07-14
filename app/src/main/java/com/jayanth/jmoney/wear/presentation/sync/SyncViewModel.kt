package com.jayanth.jmoney.wear.presentation.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jayanth.jmoney.wear.data.repository.SyncRepository
import com.jayanth.jmoney.wear.data.repository.SyncState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncRepository: SyncRepository
) : ViewModel() {

    val syncState: StateFlow<SyncState> = syncRepository.syncState

    init {
        startSyncIfNeeded()
    }

    fun startSyncIfNeeded() {
        viewModelScope.launch {
            if (!syncRepository.hasSyncedBefore()) {
                syncRepository.runFullSync()
            }
        }
    }

    fun forceFullSync() {
        viewModelScope.launch {
            syncRepository.runFullSync()
        }
    }
}
