package com.r1garage.android.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.r1garage.android.data.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val vehicleName: String? = null,
    val soc: Int? = null,
    val rangeMi: Int? = null,
    val status: String? = null,
    val odometerMi: Int? = null,
    val lastUpdatedLabel: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: VehicleRepository,
) : ViewModel() {

    val state: StateFlow<HomeUiState> = repository.latestSnapshot
        .map { snap ->
            if (snap == null) {
                HomeUiState()
            } else {
                HomeUiState(
                    vehicleName = snap.vehicleName,
                    soc = snap.socPct,
                    rangeMi = snap.rangeMi,
                    status = snap.gear,
                    odometerMi = snap.odometerMi,
                    lastUpdatedLabel = DateFormat.getDateTimeInstance().format(Date(snap.fetchedAt))
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    /**
     * Pull-to-refresh handler.
     *
     * TODO: wire this through to [VehicleRepository.refresh] once we have
     * a `vehicleId` source. The periodic poller currently relies on the
     * same missing piece (see `VehiclePollWorker.VEHICLE_ID_PLACEHOLDER`),
     * so plumbing both at once is the right move. For now this just
     * pulses the spinner so the UX pattern is in place — the cached
     * snapshot flow will still surface any fresh data the poller writes.
     */
    fun onRefresh() {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                delay(900)
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
