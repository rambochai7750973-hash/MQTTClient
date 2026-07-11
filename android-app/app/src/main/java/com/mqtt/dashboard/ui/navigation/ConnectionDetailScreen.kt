package com.mqtt.dashboard.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mqtt.dashboard.data.local.ConnectionEntity
import com.mqtt.dashboard.data.mqtt.ConnectionState
import com.mqtt.dashboard.data.mqtt.MqttConnectionConfig
import com.mqtt.dashboard.data.mqtt.MqttManager
import com.mqtt.dashboard.data.repository.MqttRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionDetailScreen(
    connectionId: Long,
    repository: MqttRepository,
    onSubscribe: () -> Unit,
    onPublish: () -> Unit,
    onDashboard: () -> Unit,
    onBack: () -> Unit
) {
    var connection by remember { mutableStateOf<ConnectionEntity?>(null) }
    val mqttManager = remember { MqttManager(com.mqtt.dashboard.MqttDashboardApp.instance) }
    val connState by mqttManager.connectionState.observeAsState()

    LaunchedEffect(connectionId) {
        val conn = withContext(Dispatchers.IO) {
            repository.database.connectionDao().getConnectionById(connectionId)
        }
        connection = conn
        if (conn != null) {
            repository.setCurrentConnectionId(conn.id)

            val config = MqttConnectionConfig(
                workerHost = conn.workerHost,
                workerPort = conn.workerPort,
                workerPath = conn.workerPath,
                brokerHost = conn.brokerHost,
                brokerPort = conn.brokerPort,
                useTls = conn.useTls,
                username = conn.username,
                password = conn.password,
                clientId = conn.clientId,
                authToken = conn.authToken,
                cleanSession = conn.cleanSession,
                connectionTimeout = conn.connectionTimeout,
                keepAliveInterval = conn.keepAliveInterval
            )
            mqttManager.connect(config)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(connection?.name ?: "Connection") },
                navigationIcon = {
                    IconButton(onClick = {
                        mqttManager.disconnect()
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            val statusText = when (connState) {
                ConnectionState.CONNECTED -> "Connected"
                ConnectionState.CONNECTING -> "Connecting..."
                ConnectionState.ERROR -> "Connection Error"
                else -> "Disconnected"
            }
            val statusColor = when (connState) {
                ConnectionState.CONNECTED -> MaterialTheme.colorScheme.secondary
                ConnectionState.ERROR -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.headlineMedium,
                color = statusColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            connection?.let { conn ->
                Text(
                    text = "wss://${conn.workerHost}:${conn.workerPort}${conn.workerPath}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onSubscribe,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Subscriptions, contentDescription = null,
                        modifier = Modifier.size(24.dp))
                    Text("Subscribe", modifier = Modifier.padding(start = 8.dp))
                }

                Button(
                    onClick = onPublish,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.Publish, contentDescription = null,
                        modifier = Modifier.size(24.dp))
                    Text("Publish", modifier = Modifier.padding(start = 8.dp))
                }

                Button(
                    onClick = onDashboard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Icon(Icons.Default.Dashboard, contentDescription = null,
                        modifier = Modifier.size(24.dp))
                    Text("Dashboard", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

private fun <T> androidx.compose.runtime.getValue(
    liveData: androidx.lifecycle.LiveData<T>
): T? {
    val state = androidx.compose.runtime.remember { mutableStateOf(liveData.value) }
    androidx.compose.runtime.DisposableEffect(liveData) {
        val observer = androidx.lifecycle.Observer<T> { state.value = it }
        liveData.observeForever(observer)
        onDispose { liveData.removeObserver(observer) }
    }
    return state.value
}
