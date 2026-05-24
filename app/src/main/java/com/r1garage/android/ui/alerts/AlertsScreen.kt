package com.r1garage.android.ui.alerts

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
import com.r1garage.android.data.local.AlertEvent
import com.r1garage.android.ui.components.ScreenScaffold
import com.r1garage.android.ui.drive.EmptyHint
import java.text.DateFormat
import java.util.Date

@Composable
fun AlertsScreen(viewModel: AlertsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ScreenScaffold(
        title = "Alerts",
        subtitle = "${state.events.size} recent events"
    ) {
        if (state.events.isEmpty()) {
            EmptyHint(
                "Smart notifications will appear here when the poller detects a " +
                    "window left down, an unexpected unlock, low 12V, vampire drain, " +
                    "or other watch-listed conditions."
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.events.forEach { AlertRow(it) }
            }
        }
    }
}

@Composable
private fun AlertRow(e: AlertEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                DateFormat.getDateTimeInstance().format(Date(e.triggeredAt)),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                e.kind,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(e.message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
