package com.r1garage.android.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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

@Composable
fun AppNavGraph() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                TopDestination.entries.forEach { dest ->
                    NavigationBarItem(
                        selected = currentRoute == dest.route,
                        onClick = {
                            nav.navigate(dest.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) }
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
        }
    }
}
