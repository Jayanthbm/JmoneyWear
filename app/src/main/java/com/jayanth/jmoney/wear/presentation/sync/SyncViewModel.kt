package com.jayanth.jmoney.wear.presentation.sync

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jayanth.jmoney.wear.data.repository.SyncRepository
import com.jayanth.jmoney.wear.data.repository.SyncState
import com.jayanth.jmoney.wear.data.work.TileUpdateHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncRepository: SyncRepository,
    @Suppress("INAPPLICABLE_JVM_NAME")
    @ApplicationContext private val context: Context
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
            TileUpdateHelper.requestAllTileUpdates(context)
        }
    }
}
