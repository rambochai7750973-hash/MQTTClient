package com.example.mqttclient.ui.screen.connection

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mqttclient.ui.components.QosSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ConnectionSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connection Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Name") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf("tcp", "ssl", "ws", "wss").forEachIndexed { index, proto ->
                    SegmentedButton(
                        selected = uiState.protocol == proto,
                        onClick = { viewModel.onProtocolChanged(proto) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 4)
                    ) { Text(proto) }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.host,
                    onValueChange = viewModel::onHostChanged,
                    modifier = Modifier.weight(3f),
                    label = { Text("Host") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = uiState.port,
                    onValueChange = viewModel::onPortChanged,
                    modifier = Modifier.weight(1f),
                    label = { Text("Port") },
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = uiState.clientId,
                    onValueChange = viewModel::onClientIdChanged,
                    modifier = Modifier.weight(1f),
                    label = { Text("Client ID") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace
                    )
                )
                IconButton(onClick = viewModel::generateClientId) {
                    Icon(Icons.Default.Refresh, contentDescription = "Generate")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.username,
                onValueChange = viewModel::onUsernameChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Username") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Advanced",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = uiState.cleanSession, onCheckedChange = viewModel::onCleanSessionChanged)
                Text("Clean Session")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = uiState.autoReconnect, onCheckedChange = viewModel::onAutoReconnectChanged)
                Text("Auto Reconnect")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Last Will",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = uiState.willEnabled, onCheckedChange = viewModel::onWillEnabledChanged)
                Text("Enable Last Will")
            }
            if (uiState.willEnabled) {
                OutlinedTextField(
                    value = uiState.willTopic,
                    onValueChange = viewModel::onWillTopicChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Will Topic") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.willPayload,
                    onValueChange = viewModel::onWillPayloadChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Will Payload") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                QosSelector(selectedQos = uiState.willQos, onQosSelected = viewModel::onWillQosChanged)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = uiState.willRetain, onCheckedChange = viewModel::onWillRetainChanged)
                    Text("Will Retain")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "TLS / SSL",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = uiState.tlsEnabled, onCheckedChange = viewModel::onTlsEnabledChanged)
                Text("Enable TLS")
            }
            if (uiState.tlsEnabled) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = uiState.tlsTrustAll, onCheckedChange = viewModel::onTlsTrustAllChanged)
                    Text("Trust all certificates")
                }
            }

            uiState.error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            uiState.connectResult?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = viewModel::saveAndConnect,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Save & Connect")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
