package com.r1garage.android.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.r1garage.android.ui.alerts.AlertsScreen
import com.r1garage.android.ui.charge.ChargeScreen
import com.r1garage.android.ui.drive.DriveScreen
import com.r1garage.android.ui.garage.GarageScreen
import com.r1garage.android.ui.home.HomeScreen
import com.r1garage.android.ui.settings.SettingsScreen

@Composable
fun AppNavGraph() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // Flat bottom bar on the page background — no elevation tint —
            // matches the Rivian app's edge-to-edge "the nav is part of the
            // page" feel.
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp,
            ) {
                TopDestination.entries.forEach { dest ->
                    val selected = currentRoute == dest.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            nav.navigate(dest.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = {
                            Text(
                                dest.label,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = Color.Transparent,
                        ),
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = TopDestination.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(TopDestination.Home.route) { HomeScreen() }
            composable(TopDestination.Drive.route) { DriveScreen() }
            composable(TopDestination.Charge.route) { ChargeScreen() }
            composable(TopDestination.Alerts.route) { AlertsScreen() }
            composable(TopDestination.Garage.route) { GarageScreen() }
            composable(TopDestination.Settings.route) { SettingsScreen() }
        }
    }
}
