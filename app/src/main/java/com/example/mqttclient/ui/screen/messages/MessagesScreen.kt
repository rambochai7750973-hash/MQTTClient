package com.example.mqttclient.ui.screen.messages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mqttclient.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mqttclient.data.model.ConnectionState
import com.example.mqttclient.ui.components.ConnectionStatusBar
import com.example.mqttclient.ui.components.EmptyStateView
import com.example.mqttclient.ui.components.MessageCard
import com.example.mqttclient.ui.components.PayloadBottomSheet

@Composable
fun MessagesScreen(
    onNavigateToConnectionSettings: () -> Unit,
    viewModel: MessagesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty() && listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index != uiState.messages.size - 1) {
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ConnectionStatusBar(
            state = uiState.connectionState,
            onClick = {
                if (uiState.connectionState is ConnectionState.Disconnected) {
                    onNavigateToConnectionSettings()
                }
            }
        )

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            placeholder = { Text(stringResource(R.string.search_topic_placeholder)) },
            singleLine = true,
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear))
                    }
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.messages_count, uiState.messages.size),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (uiState.messages.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.clear_all),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.messages.isEmpty()) {
                if (uiState.connectionState is ConnectionState.Disconnected) {
                    EmptyStateView(
                        icon = Icons.Default.CloudOff,
                        title = stringResource(R.string.not_connected),
                        subtitle = stringResource(R.string.connect_hint),
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    EmptyStateView(
                        icon = Icons.Default.Inbox,
                        title = stringResource(R.string.no_messages),
                        subtitle = stringResource(R.string.subscribe_hint),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else {
                val filteredMessages = if (uiState.searchQuery.isBlank()) {
                    uiState.messages
                } else {
                    uiState.messages.filter { it.topic.contains(uiState.searchQuery, ignoreCase = true) }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredMessages, key = { it.id }) { message ->
                        MessageCard(
                            message = message,
                            onClick = { viewModel.onMessageClick(message) },
                            onLongClick = { viewModel.onMessageClick(message) }
                        )
                    }
                }
            }
        }
    }

    PayloadBottomSheet(
        message = uiState.selectedMessage,
        onDismiss = viewModel::onDismissSheet
    )
}
