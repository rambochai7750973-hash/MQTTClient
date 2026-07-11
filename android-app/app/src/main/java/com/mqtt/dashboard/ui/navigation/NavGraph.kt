package com.mqtt.dashboard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mqtt.dashboard.data.local.ConnectionEntity
import com.mqtt.dashboard.data.repository.MqttRepository
import com.mqtt.dashboard.ui.connection.ConnectionEditScreen
import com.mqtt.dashboard.ui.connection.ConnectionListScreen
import com.mqtt.dashboard.ui.dashboard.DashboardScreen
import com.mqtt.dashboard.ui.publish.PublishScreen
import com.mqtt.dashboard.ui.subscribe.SubscribeScreen

object Routes {
    const val CONNECTION_LIST = "connection_list"
    const val CONNECTION_EDIT = "connection_edit?connectionId={connectionId}"
    const val CONNECTION_DETAIL = "connection_detail/{connectionId}"
    const val SUBSCRIBE = "subscribe/{connectionId}"
    const val PUBLISH = "publish/{connectionId}"
    const val DASHBOARD = "dashboard/{connectionId}"

    fun connectionEdit(id: Long? = null) =
        if (id != null) "connection_edit?connectionId=$id" else "connection_edit"

    fun connectionDetail(id: Long) = "connection_detail/$id"
    fun subscribe(id: Long) = "subscribe/$id"
    fun publish(id: Long) = "publish/$id"
    fun dashboard(id: Long) = "dashboard/$id"
}

data class ConnectionState(
    val activeConnection: ConnectionEntity? = null,
    val activeConnectionId: Long? = null
)

@Composable
fun MqttNavGraph(
    navController: NavHostController,
    repository: MqttRepository
) {
    var connectionState by remember { mutableStateOf(ConnectionState()) }

    NavHost(navController = navController, startDestination = Routes.CONNECTION_LIST) {
        composable(Routes.CONNECTION_LIST) {
            ConnectionListScreen(
                repository = repository,
                onAddConnection = { navController.navigate(Routes.connectionEdit(null)) },
                onEditConnection = { id -> navController.navigate(Routes.connectionEdit(id)) },
                onConnect = { conn ->
                    connectionState = connectionState.copy(
                        activeConnection = conn, activeConnectionId = conn.id
                    )
                    navController.navigate(Routes.connectionDetail(conn.id))
                }
            )
        }

        composable(
            route = "connection_edit?connectionId={connectionId}",
            arguments = listOf(navArgument("connectionId") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { backStackEntry ->
            val connectionId = backStackEntry.arguments?.getLong("connectionId") ?: -1L
            ConnectionEditScreen(
                connectionId = if (connectionId == -1L) null else connectionId,
                repository = repository,
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = "connection_detail/{connectionId}",
            arguments = listOf(navArgument("connectionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val connectionId = backStackEntry.arguments?.getLong("connectionId") ?: return@composable
            ConnectionDetailScreen(
                connectionId = connectionId,
                repository = repository,
                onSubscribe = { navController.navigate(Routes.subscribe(connectionId)) },
                onPublish = { navController.navigate(Routes.publish(connectionId)) },
                onDashboard = { navController.navigate(Routes.dashboard(connectionId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "subscribe/{connectionId}",
            arguments = listOf(navArgument("connectionId") { type = NavType.LongType })
        ) {
            SubscribeScreen(
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "publish/{connectionId}",
            arguments = listOf(navArgument("connectionId") { type = NavType.LongType })
        ) {
            PublishScreen(
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "dashboard/{connectionId}",
            arguments = listOf(navArgument("connectionId") { type = NavType.LongType })
        ) {
            DashboardScreen(
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
