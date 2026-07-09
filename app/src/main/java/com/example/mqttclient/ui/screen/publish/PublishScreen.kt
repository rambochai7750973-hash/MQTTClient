package com.example.mqttclient.ui.screen.publish

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import com.example.mqttclient.data.model.ConnectionState
import com.example.mqttclient.ui.components.QosSelector
import com.example.mqttclient.ui.components.TopicAutoCompleteField

@Composable
fun PublishScreen(
    viewModel: PublishViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        TopicAutoCompleteField(
            value = uiState.topic,
            onValueChange = viewModel::onTopicChanged,
            suggestions = uiState.recentTopics,
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(R.string.topic)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.payload,
            onValueChange = viewModel::onPayloadChanged,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            label = { Text(stringResource(R.string.payload)) },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace
            ),
            maxLines = 10
        )

        Spacer(modifier = Modifier.height(8.dp))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = !uiState.isPayloadHex,
                onClick = { viewModel.onPayloadModeChanged(false) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) { Text(stringResource(R.string.text_mode)) }
            SegmentedButton(
                selected = uiState.isPayloadHex,
                onClick = { viewModel.onPayloadModeChanged(true) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) { Text(stringResource(R.string.hex_mode)) }
        }

        Spacer(modifier = Modifier.height(12.dp))

        QosSelector(
            selectedQos = uiState.qos,
            onQosSelected = viewModel::onQosChanged,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.retain),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = uiState.retain,
                onCheckedChange = viewModel::onRetainChanged
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = viewModel::publish,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = uiState.topic.isNotBlank() &&
                    uiState.payload.isNotBlank() &&
                    uiState.connectionState is ConnectionState.Connected
        ) {
            Icon(Icons.Default.Send, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.publish))
        }

        uiState.publishSuccess?.let { success ->
            Spacer(modifier = Modifier.height(8.dp))
            if (success) {
                Text(
                    text = stringResource(R.string.published_success),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = stringResource(R.string.failed) + (uiState.publishError?.let { ": $it" } ?: ""),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
