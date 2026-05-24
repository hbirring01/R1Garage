package com.r1garage.android.ui.garage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.r1garage.android.data.local.Mod
import com.r1garage.android.ui.components.ScreenScaffold
import com.r1garage.android.ui.components.StatCard
import com.r1garage.android.ui.drive.EmptyHint
import java.text.DateFormat
import java.util.Date

@Composable
fun GarageScreen(viewModel: GarageViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ScreenScaffold(
        title = "Garage",
        subtitle = "Service · Mods · Recalls"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                label = "NEXT SERVICE",
                value = state.nextServiceLabel ?: "—",
                sublabel = state.odometerMi?.let { "$it mi" }
            )
            StatCard(
                label = "OPEN RECALLS",
                value = state.openRecalls.toString(),
                sublabel = "NHTSA"
            )
            Text(
                "Modifications",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (state.mods.isEmpty()) {
                EmptyHint("Track lifts, wheels, lights, accessories, and software updates here.")
            } else {
                state.mods.forEach { ModRow(it) }
            }
        }
    }
}

@Composable
private fun ModRow(m: Mod) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(m.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "Installed ${DateFormat.getDateInstance().format(Date(m.installedAt))} · ${m.odometerMi} mi",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!m.notes.isNullOrBlank()) {
                Text(
                    m.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
