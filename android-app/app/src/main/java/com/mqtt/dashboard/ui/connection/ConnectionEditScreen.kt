package com.mqtt.dashboard.ui.connection

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mqtt.dashboard.data.local.ConnectionEntity
import com.mqtt.dashboard.data.repository.MqttRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionEditScreen(
    connectionId: Long?,
    repository: MqttRepository,
    onSaved: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var workerHost by remember { mutableStateOf("") }
    var workerPort by remember { mutableStateOf("443") }
    var workerPath by remember { mutableStateOf("/mqtt") }
    var brokerHost by remember { mutableStateOf("") }
    var brokerPort by remember { mutableStateOf("") }
    var useTls by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var clientId by remember { mutableStateOf("android_" + System.currentTimeMillis().toString(16)) }
    var authToken by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val isEditing = connectionId != null

    LaunchedEffect(connectionId) {
        if (connectionId != null) {
            val conn = withContext(Dispatchers.IO) {
                repository.database.connectionDao().getConnectionById(connectionId)
            }
            conn?.let {
                name = it.name
                workerHost = it.workerHost
                workerPort = it.workerPort.toString()
                workerPath = it.workerPath
                brokerHost = it.brokerHost
                brokerPort = it.brokerPort
                useTls = it.useTls
                username = it.username
                password = it.password
                clientId = it.clientId
                authToken = it.authToken
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Connection" else "New Connection") },
                navigationIcon = {
                    IconButton(onClick = onSaved) {
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Connection Name") },
                placeholder = { Text("ESP8266 Monitor") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = workerHost,
                onValueChange = { workerHost = it },
                label = { Text("Worker Host") },
                placeholder = { Text("mqtt-websocket-bridge.xxx.workers.dev") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = workerPort,
                onValueChange = { workerPort = it },
                label = { Text("Worker Port") },
                placeholder = { Text("443") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = workerPath,
                onValueChange = { workerPath = it },
                label = { Text("WebSocket Path") },
                placeholder = { Text("/mqtt") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Optional: Direct Broker Override", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = brokerHost,
                onValueChange = { brokerHost = it },
                label = { Text("Broker Host (override)") },
                placeholder = { Text("broker.emqx.io") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = brokerPort,
                onValueChange = { brokerPort = it },
                label = { Text("Broker Port (override)") },
                placeholder = { Text("1883") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("TLS", modifier = Modifier.weight(1f))
                Switch(checked = useTls, onCheckedChange = { useTls = it })
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Authentication", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("MQTT Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("MQTT Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = authToken,
                onValueChange = { authToken = it },
                label = { Text("Worker Auth Token") },
                placeholder = { Text("Bearer token (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = clientId,
                onValueChange = { clientId = it },
                label = { Text("Client ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            val entity = ConnectionEntity(
                                id = connectionId ?: 0,
                                name = name,
                                workerHost = workerHost,
                                workerPort = workerPort.toIntOrNull() ?: 443,
                                workerPath = workerPath,
                                brokerHost = brokerHost,
                                brokerPort = brokerPort,
                                useTls = useTls,
                                username = username,
                                password = password,
                                clientId = clientId,
                                authToken = authToken
                            )
                            if (isEditing) {
                                repository.updateConnection(entity)
                            } else {
                                repository.saveConnection(entity)
                            }
                        }
                        onSaved()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Text("Save", modifier = Modifier.padding(start = 8.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
