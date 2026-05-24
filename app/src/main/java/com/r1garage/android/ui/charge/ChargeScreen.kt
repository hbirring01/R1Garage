package com.r1garage.android.ui.charge

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
import com.r1garage.android.data.local.ChargeSession
import com.r1garage.android.ui.components.ScreenScaffold
import com.r1garage.android.ui.components.StatCard
import com.r1garage.android.ui.drive.EmptyHint
import java.text.DateFormat
import java.util.Date

@Composable
fun ChargeScreen(viewModel: ChargeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ScreenScaffold(
        title = "Charge",
        subtitle = "${state.sessionCount} sessions · ${"%.0f".format(state.lifetimeKwh)} kWh added"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                label = "OPTIMIZER",
                value = state.optimizerHint ?: "Set up TOU plan",
                sublabel = "Tap Settings"
            )
            Text(
                "Recent sessions",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (state.recent.isEmpty()) {
                EmptyHint("Charging sessions auto-detected from plug + SoC changes will appear here.")
            } else {
                state.recent.forEach { SessionRow(it) }
            }
        }
    }
}

@Composable
private fun SessionRow(s: ChargeSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                DateFormat.getDateTimeInstance().format(Date(s.startedAt)),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "+%.1f kWh · %d%% → %d%%".format(s.kwhAdded, s.startSoc, s.endSoc),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                "peak ${"%.0f".format(s.peakKw)} kW",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
