package com.r1garage.android.ui.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.r1garage.android.data.local.AlertEvent
import com.r1garage.android.data.local.AlertEventDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class AlertsUiState(val events: List<AlertEvent> = emptyList())

@HiltViewModel
class AlertsViewModel @Inject constructor(
    dao: AlertEventDao,
) : ViewModel() {
    val state: StateFlow<AlertsUiState> = dao.observeRecent()
        .map { AlertsUiState(events = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AlertsUiState())
}
