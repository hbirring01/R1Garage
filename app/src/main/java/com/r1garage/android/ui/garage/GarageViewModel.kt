package com.r1garage.android.ui.garage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.r1garage.android.data.local.Mod
import com.r1garage.android.data.local.ModDao
import com.r1garage.android.data.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class GarageUiState(
    val odometerMi: Int? = null,
    val nextServiceLabel: String? = null,
    val openRecalls: Int = 0,
    val mods: List<Mod> = emptyList(),
)

@HiltViewModel
class GarageViewModel @Inject constructor(
    modDao: ModDao,
    repository: VehicleRepository,
) : ViewModel() {

    val state: StateFlow<GarageUiState> =
        combine(modDao.observeAll(), repository.latestSnapshot) { mods, snap ->
            val odo = snap?.odometerMi
            GarageUiState(
                odometerMi = odo,
                nextServiceLabel = odo?.let { nextService(it) },
                openRecalls = 0,
                mods = mods,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GarageUiState())

    private fun nextService(odometerMi: Int): String {
        // Simple rule of thumb until we wire real Rivian intervals.
        val nextRotation = ((odometerMi / 7_500) + 1) * 7_500
        val remaining = nextRotation - odometerMi
        return "Tire rotation in $remaining mi"
    }
}
