package com.r1garage.android.ui.charge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.r1garage.android.data.local.ChargeSession
import com.r1garage.android.data.local.ChargeSessionDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ChargeUiState(
    val sessionCount: Int = 0,
    val lifetimeKwh: Double = 0.0,
    val optimizerHint: String? = null,
    val recent: List<ChargeSession> = emptyList(),
)

@HiltViewModel
class ChargeViewModel @Inject constructor(
    dao: ChargeSessionDao,
) : ViewModel() {

    val state: StateFlow<ChargeUiState> = dao.observeAll()
        .map { sessions ->
            ChargeUiState(
                sessionCount = sessions.size,
                lifetimeKwh = sessions.sumOf { it.kwhAdded },
                recent = sessions.take(10)
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChargeUiState())
}
