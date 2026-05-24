package com.r1garage.android.ui.drive

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
import com.r1garage.android.data.local.Trip
import com.r1garage.android.ui.components.ScreenScaffold
import com.r1garage.android.ui.components.StatCard
import java.text.DateFormat
import java.util.Date

@Composable
fun DriveScreen(viewModel: DriveViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ScreenScaffold(
        title = "Drive",
        subtitle = "${state.tripCount} trips · lifetime ${state.lifetimeMi} mi"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                label = "AVG EFFICIENCY",
                value = state.avgEfficiency?.let { "%.2f mi/kWh".format(it) } ?: "—"
            )
            Text(
                "Recent trips",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (state.recentTrips.isEmpty()) {
                EmptyHint("Trips will appear here once you drive with the app installed.")
            } else {
                state.recentTrips.forEach { TripRow(it) }
            }
        }
    }
}

@Composable
private fun TripRow(trip: Trip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                DateFormat.getDateTimeInstance().format(Date(trip.startedAt)),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "%.1f mi · %.1f kWh".format(trip.distanceMi, trip.kwhUsed),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                "%.2f mi/kWh".format(trip.efficiency()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun EmptyHint(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
        )
    }
}
