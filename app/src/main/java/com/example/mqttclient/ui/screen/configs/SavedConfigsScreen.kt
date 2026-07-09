package com.example.mqttclient.ui.screen.configs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.example.mqttclient.data.model.ConnectionConfig
import com.example.mqttclient.ui.components.ConfirmDialog
import com.example.mqttclient.ui.components.EmptyStateView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedConfigsScreen(
    onNavigateBack: () -> Unit,
    onEditConfig: (Long) -> Unit,
    viewModel: SavedConfigsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.saved_configs)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.nav_back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onEditConfig(-1L) }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_label))
            }
        }
    ) { padding ->
        if (uiState.configs.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.CloudQueue,
                title = stringResource(R.string.no_configs),
                subtitle = stringResource(R.string.tap_to_add),
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.configs, key = { it.id }) { config ->
                    ConfigCard(
                        config = config,
                        onClick = { viewModel.connect(config) },
                        onEdit = { onEditConfig(config.id) },
                        onDelete = { viewModel.showDeleteConfirm(config) }
                    )
                }
            }
        }

        uiState.showDeleteConfirm?.let { config ->
            ConfirmDialog(
                title = stringResource(R.string.delete_config_title),
                message = stringResource(R.string.delete_config_message, config.name),
                confirmText = stringResource(R.string.delete),
                onConfirm = {
                    viewModel.deleteConfig(config)
                    viewModel.dismissDeleteConfirm()
                },
                onDismiss = viewModel::dismissDeleteConfirm
            )
        }
    }
}

@Composable
private fun ConfigCard(
    config: ConnectionConfig,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = config.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${config.protocol}://${config.host}:${config.port}",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.edit_label))
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
