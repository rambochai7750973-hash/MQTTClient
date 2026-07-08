package com.example.mqttclient.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mqttclient.data.model.ConnectionState
import com.example.mqttclient.ui.theme.ConnectedGreen
import com.example.mqttclient.ui.theme.ConnectingYellow
import com.example.mqttclient.ui.theme.DisconnectedRed
import com.example.mqttclient.ui.theme.InactiveGrey

@Composable
fun ConnectionStatusBar(
    state: ConnectionState,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val (color, text) = when (state) {
        is ConnectionState.Connected -> ConnectedGreen to "Connected"
        is ConnectionState.Connecting -> ConnectingYellow to "Connecting"
        is ConnectionState.Disconnecting -> ConnectingYellow to "Disconnecting"
        is ConnectionState.Disconnected -> InactiveGrey to "Disconnected"
        is ConnectionState.Error -> DisconnectedRed to "Error"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (state is ConnectionState.Connected) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = state.serverUri,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = when (state) {
                    is ConnectionState.Connected -> "Disconnect"
                    is ConnectionState.Disconnected -> "Connect"
                    else -> ""
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (state is ConnectionState.Error) {
            Text(
                text = state.reason,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = DisconnectedRed
            )
        }
    }
}
