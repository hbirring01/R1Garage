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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val vehicleName: String? = null,
    val vehicleImageUrl: String? = null,
    val soc: Int? = null,
    val rangeMi: Int? = null,
    val status: String? = null,
    val odometerMi: Int? = null,
    val lastUpdatedLabel: String? = null,
    /**
     * Non-null when the most recent bootstrap / refresh attempt failed.
     * Shown in a small banner on Home so the user (and bug reports) can
     * see why the screen is empty instead of staring at "Loading…".
     */
    val errorMessage: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: VehicleRepository,
    private val tokenStore: RivianTokenStore,
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)

    val state: StateFlow<HomeUiState> = combine(repository.latestSnapshot, _error) { snap, err ->
        if (snap == null) {
            HomeUiState(
                vehicleName = tokenStore.vehicleName,
                vehicleImageUrl = tokenStore.vehicleImageUrl,
                errorMessage = err,
            )
        } else {
            HomeUiState(
                vehicleName = snap.vehicleName ?: tokenStore.vehicleName,
                vehicleImageUrl = tokenStore.vehicleImageUrl,
                soc = snap.socPct,
                rangeMi = snap.rangeMi,
                status = snap.gear,
                odometerMi = snap.odometerMi,
                lastUpdatedLabel = DateFormat.getDateTimeInstance().format(Date(snap.fetchedAt)),
                errorMessage = err,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_SUBSCRIPTION_TIMEOUT_MS), HomeUiState())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        // On first composition after sign-in we may not yet have a vehicleId.
        // Discover it (one-shot) and pull an initial snapshot so the Home
        // screen isn't empty.
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

    @Suppress("TooGenericExceptionCaught")
    private suspend fun bootstrapAndRefresh() {
        if (!tokenStore.isSignedIn) {
            _error.value = "Not signed in"
            return
        }
        // Resolve a vehicleId — cached one wins, otherwise discover via
        // GetUserInfo. Surface enrollment failures to the UI so the user
        // can see why Home is empty instead of being stuck on "Loading…".
        val vehicleId = try {
            tokenStore.vehicleId ?: repository.enrollFirstVehicle()
        } catch (t: Throwable) {
            _error.value = "Couldn't load your vehicle: ${t.message ?: t.javaClass.simpleName}"
            return
        }
        if (vehicleId.isNullOrBlank()) {
            _error.value = "No vehicle on this Rivian account"
            return
        }
        // Backfill the vehicle image on upgrade: if we already had a
        // vehicleId cached (from v0.1.3 / v0.1.4) we skipped
        // enrollFirstVehicle() and therefore never fetched the image.
        if (tokenStore.vehicleImageUrl.isNullOrBlank()) {
            try {
                repository.refreshVehicleImage(vehicleId)
            } catch (_: Throwable) {
                // non-fatal: the silhouette fallback is still fine
            }
        }
        // Pull a fresh snapshot. This is the call that populates SOC /
        // range / odometer; bubble its error up so empty-Home isn't silent.
        try {
            repository.refresh(vehicleId)
            _error.value = null
        } catch (t: Throwable) {
            _error.value = "Couldn't fetch vehicle status: ${t.message ?: t.javaClass.simpleName}"
        }
    }

    private companion object {
        /**
         * Keep the upstream snapshot/error flows hot for 5s after the last
         * subscriber goes away — long enough to survive a config change
         * without re-subscribing, short enough to release resources when
         * the user backs out of Home.
         */
        const val STATE_SUBSCRIPTION_TIMEOUT_MS = 5_000L
    }
}
