package com.r1garage.android.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.r1garage.android.data.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

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
}
