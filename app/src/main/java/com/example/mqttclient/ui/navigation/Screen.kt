package com.example.mqttclient.ui.navigation

sealed class Screen(val route: String) {
    data object Messages : Screen("messages")
    data object Subscriptions : Screen("subscriptions")
    data object Publish : Screen("publish")
    data object Settings : Screen("settings")
    data object ConnectionSettings : Screen("connection_settings/{configId}") {
        fun createRoute(configId: Long = -1L) = "connection_settings/$configId"
    }
    data object SavedConfigs : Screen("saved_configs")
    data object Log : Screen("log")
    data object About : Screen("about")
}
