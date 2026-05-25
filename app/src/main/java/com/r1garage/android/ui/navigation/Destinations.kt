package com.r1garage.android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class TopDestination(val route: String, val label: String, val icon: ImageVector) {
    Home("home", "Now", Icons.Filled.Home),
    Drive("drive", "Drive", Icons.Filled.DirectionsCar),
    Charge("charge", "Charge", Icons.Filled.Bolt),
    Alerts("alerts", "Alerts", Icons.Filled.NotificationsActive),
    Garage("garage", "Garage", Icons.Filled.Build),
    Settings("settings", "Settings", Icons.Filled.Settings),
}
