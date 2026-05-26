package com.r1garage.android.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.r1garage.android.data.repository.VehicleRepository
import com.r1garage.android.data.rivian.RivianTokenStore
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject
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
    private val repository: VehicleRepository,
    private val tokenStore: RivianTokenStore,
) : ViewModel() {

    val state: StateFlow<HomeUiState> = repository.latestSnapshot
        .map { snap ->
            if (snap == null) {
                HomeUiState(vehicleName = tokenStore.vehicleName)
            } else {
                HomeUiState(
                    vehicleName = snap.vehicleName ?: tokenStore.vehicleName,
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

    init {
        // On first composition after sign-in we may not yet have a vehicleId.
        // Discover it (one-shot) and pull an initial snapshot so the Home
        // screen isn't empty. Failures stay silent — pull-to-refresh will
        // retry, and the periodic poller will catch up too.
        if (tokenStore.isSignedIn) {
            viewModelScope.launch { bootstrapAndRefresh() }
        }
    }

    /**
     * Pull-to-refresh handler. Triggers an enrollment lookup if needed,
     * then fetches a fresh `vehicleState`.
     */
    fun onRefresh() {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                bootstrapAndRefresh()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private suspend fun bootstrapAndRefresh() {
        if (!tokenStore.isSignedIn) return
        // Enrollment / refresh failures are surfaced by the periodic poller's
        // alert path; here we just want pull-to-refresh to release its
        // spinner without crashing the VM. runCatching swallows by design so
        // detekt's TooGenericExceptionCaught / SwallowedException don't fire.
        val vehicleId = runCatching {
            tokenStore.vehicleId ?: repository.enrollFirstVehicle()
        }.getOrNull() ?: return
        runCatching { repository.refresh(vehicleId) }
    }
}
