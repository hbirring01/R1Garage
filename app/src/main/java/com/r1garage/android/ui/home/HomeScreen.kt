package com.r1garage.android.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.r1garage.android.ui.components.BatteryHero
import com.r1garage.android.ui.components.StatCard
import com.r1garage.android.ui.components.StatusPill

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        // Greeting / vehicle name — small, muted. The hero readout below
        // does the heavy lifting visually.
        Text(
            text = state.vehicleName ?: "Not signed in",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(28.dp))
        BatteryHero(socPct = state.soc, rangeMi = state.rangeMi)
        Spacer(Modifier.height(28.dp))

        // Status pills row — read-only state indicators (R1Garage is a
        // read-only companion; we don't issue lock/unlock commands).
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatusPill(
                icon = Icons.Outlined.Lock,
                label = state.status ?: "—",
                modifier = Modifier.weight(1f, fill = false),
            )
            if (state.odometerMi != null) {
                StatusPill(
                    icon = Icons.Outlined.Speed,
                    label = "${state.odometerMi} mi",
                    modifier = Modifier.weight(1f, fill = false),
                )
            }
            StatusPill(
                icon = Icons.Outlined.LocationOn,
                label = "Parked",
                modifier = Modifier.weight(1f, fill = false),
            )
        }

        Spacer(Modifier.height(28.dp))

        // Detail cards — flat outlined, kept for telemetry that doesn't fit
        // in the hero/pill area.
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatCard(
                    label = "STATE OF CHARGE",
                    value = state.soc?.let { "$it%" } ?: "—",
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = "RANGE",
                    value = state.rangeMi?.let { "$it mi" } ?: "—",
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatCard(
                    label = "STATUS",
                    value = state.status ?: "—",
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = "ODOMETER",
                    value = state.odometerMi?.let { "$it mi" } ?: "—",
                    modifier = Modifier.weight(1f),
                )
            }
            StatCard(
                label = "LAST UPDATE",
                value = state.lastUpdatedLabel ?: "Never",
                sublabel = "Pull-to-refresh coming soon",
            )
        }
    }
}
