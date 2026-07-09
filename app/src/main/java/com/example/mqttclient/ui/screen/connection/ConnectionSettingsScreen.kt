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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.mqttclient.R
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
                title = { Text(stringResource(R.string.connection_settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.nav_back))
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
                label = { Text(stringResource(R.string.name_label)) },
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
                    label = { Text(stringResource(R.string.host)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = uiState.port,
                    onValueChange = viewModel::onPortChanged,
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.port)) },
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = uiState.clientId,
                    onValueChange = viewModel::onClientIdChanged,
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.client_id)) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace
                    )
                )
                IconButton(onClick = viewModel::generateClientId) {
                    Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.generate))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.username,
                onValueChange = viewModel::onUsernameChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.username)) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.password)) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.advanced),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = uiState.cleanSession, onCheckedChange = viewModel::onCleanSessionChanged)
                Text(stringResource(R.string.clean_session))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = uiState.autoReconnect, onCheckedChange = viewModel::onAutoReconnectChanged)
                Text(stringResource(R.string.auto_reconnect))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.last_will),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = uiState.willEnabled, onCheckedChange = viewModel::onWillEnabledChanged)
                Text(stringResource(R.string.enable_last_will))
            }
            if (uiState.willEnabled) {
                OutlinedTextField(
                    value = uiState.willTopic,
                    onValueChange = viewModel::onWillTopicChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.will_topic)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.willPayload,
                    onValueChange = viewModel::onWillPayloadChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.will_payload)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                QosSelector(selectedQos = uiState.willQos, onQosSelected = viewModel::onWillQosChanged)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = uiState.willRetain, onCheckedChange = viewModel::onWillRetainChanged)
                    Text(stringResource(R.string.will_retain))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.tls_ssl_label),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = uiState.tlsEnabled, onCheckedChange = viewModel::onTlsEnabledChanged)
                Text(stringResource(R.string.enable_tls))
            }
            if (uiState.tlsEnabled) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = uiState.tlsTrustAll, onCheckedChange = viewModel::onTlsTrustAllChanged)
                    Text(stringResource(R.string.trust_all_certs))
                }
            }

            if (uiState.validationError) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = stringResource(R.string.name_host_required), color = MaterialTheme.colorScheme.error)
            }

            uiState.connectSuccess?.let { success ->
                Spacer(modifier = Modifier.height(8.dp))
                if (success) {
                    Text(text = stringResource(R.string.connected_success), color = MaterialTheme.colorScheme.primary)
                } else {
                    Text(
                        text = stringResource(R.string.failed) + (uiState.connectionError?.let { ": $it" } ?: ""),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = viewModel::saveAndConnect,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(stringResource(R.string.save_and_connect))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
