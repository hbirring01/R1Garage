package com.r1garage.android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Garage
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

// Bottom-nav destinations. Outlined icons mirror the line-icon style used
// throughout the official Rivian companion app.
enum class TopDestination(val route: String, val label: String, val icon: ImageVector) {
    Home("home", "Home", Icons.Outlined.Home),
    Drive("drive", "Drive", Icons.Outlined.DirectionsCar),
    Charge("charge", "Charge", Icons.Outlined.Bolt),
    Alerts("alerts", "Alerts", Icons.Outlined.NotificationsNone),
    Garage("garage", "Garage", Icons.Outlined.Garage),
    Settings("settings", "Settings", Icons.Outlined.Settings),
}
