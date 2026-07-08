package com.example.mqttclient.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mqttclient.ui.screen.configs.SavedConfigsScreen
import com.example.mqttclient.ui.screen.connection.ConnectionSettingsScreen
import com.example.mqttclient.ui.screen.messages.MessagesScreen
import com.example.mqttclient.ui.screen.publish.PublishScreen
import com.example.mqttclient.ui.screen.settings.SettingsScreen
import com.example.mqttclient.ui.screen.subscriptions.SubscriptionsScreen

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen
)

val bottomNavItems = listOf(
    BottomNavItem("Messages", Icons.Default.Email, Screen.Messages),
    BottomNavItem("Subscriptions", Icons.Default.Subscriptions, Screen.Subscriptions),
    BottomNavItem("Publish", Icons.Default.Send, Screen.Publish),
    BottomNavItem("Settings", Icons.Default.Settings, Screen.Settings)
)

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.screen.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == item.screen.route
                        } == true
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Messages.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Messages.route) {
                MessagesScreen(
                    onNavigateToConnectionSettings = {
                        navController.navigate(Screen.ConnectionSettings.createRoute())
                    }
                )
            }
            composable(Screen.Subscriptions.route) {
                SubscriptionsScreen()
            }
            composable(Screen.Publish.route) {
                PublishScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToConfigs = { navController.navigate(Screen.SavedConfigs.route) },
                    onNavigateToLogs = { navController.navigate(Screen.Log.route) }
                )
            }
            composable(
                route = Screen.ConnectionSettings.route,
                arguments = listOf(navArgument("configId") {
                    type = NavType.LongType
                    defaultValue = -1L
                })
            ) {
                ConnectionSettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.SavedConfigs.route) {
                SavedConfigsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onEditConfig = { configId ->
                        navController.navigate(Screen.ConnectionSettings.createRoute(configId))
                    }
                )
            }
            composable(Screen.Log.route) {
                com.example.mqttclient.ui.screen.log.LogScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
