package com.r1garage.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Hero readout that anchors the Home screen — emulates the official Rivian
 * app's giant battery percentage with the estimated range right below it.
 *
 * Renders huge "84" + small "%" baseline, then "284 mi" muted underneath.
 */
@Composable
fun BatteryHero(
    socPct: Int?,
    rangeMi: Int?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                text = socPct?.toString() ?: "—",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Light,
                ),
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (socPct != null) {
                Text(
                    text = "%",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Light,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 18.dp, start = 4.dp),
                )
            }
        }
        Text(
            text = rangeMi?.let { "$it mi estimated range" } ?: "Range unavailable",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
