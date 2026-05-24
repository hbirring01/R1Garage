package com.r1garage.android.ui.drive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.r1garage.android.data.local.Trip
import com.r1garage.android.data.local.TripDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class DriveUiState(
    val tripCount: Int = 0,
    val lifetimeMi: Int = 0,
    val avgEfficiency: Double? = null,
    val recentTrips: List<Trip> = emptyList(),
)

@HiltViewModel
class DriveViewModel @Inject constructor(
    tripDao: TripDao,
) : ViewModel() {

    val state: StateFlow<DriveUiState> = tripDao.observeAll()
        .map { trips ->
            val totalMi = trips.sumOf { it.distanceMi }
            val totalKwh = trips.sumOf { it.kwhUsed }
            DriveUiState(
                tripCount = trips.size,
                lifetimeMi = totalMi.toInt(),
                avgEfficiency = if (totalKwh > 0.01) totalMi / totalKwh else null,
                recentTrips = trips.take(10)
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DriveUiState())
}
