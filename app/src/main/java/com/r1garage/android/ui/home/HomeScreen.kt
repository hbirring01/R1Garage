package com.r1garage.android.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.r1garage.android.ui.components.ScreenScaffold
import com.r1garage.android.ui.components.StatCard

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ScreenScaffold(
        title = "Now",
        subtitle = state.vehicleName ?: "Not signed in"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "STATE OF CHARGE",
                    value = state.soc?.let { "${it}%" } ?: "—",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "RANGE",
                    value = state.rangeMi?.let { "$it mi" } ?: "—",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "STATUS",
                    value = state.status ?: "—",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "ODOMETER",
                    value = state.odometerMi?.let { "$it mi" } ?: "—",
                    modifier = Modifier.weight(1f)
                )
            }
            StatCard(
                label = "LAST UPDATE",
                value = state.lastUpdatedLabel ?: "Never",
                sublabel = "Pull-to-refresh coming soon"
            )
        }
    }
}
