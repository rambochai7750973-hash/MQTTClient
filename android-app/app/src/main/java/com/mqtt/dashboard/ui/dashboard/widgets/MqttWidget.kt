package com.mqtt.dashboard.ui.dashboard.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.mqtt.dashboard.ui.theme.Blue800
import com.mqtt.dashboard.ui.theme.Green500
import com.mqtt.dashboard.ui.theme.Red500
import com.mqtt.dashboard.ui.theme.Teal400
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun MqttWidget(
    type: WidgetType,
    name: String,
    topic: String,
    configJson: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = topic,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            when (type) {
                WidgetType.GAUGE -> GaugeContent()
                WidgetType.TEXT -> TextContent()
                WidgetType.BUTTON -> ButtonContent()
                WidgetType.SWITCH -> SwitchContent()
                WidgetType.CHART -> ChartContent()
                WidgetType.LED -> LedContent()
            }
        }
    }
}

@Composable
private fun GaugeContent() {
    val value = 25.5f
    val maxValue = 50f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(100.dp)) {
            val strokeWidth = 12f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            drawArc(
                color = Color(0xFFE0E0E0),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            val sweep = (value / maxValue) * 270f
            drawArc(
                color = Blue800,
                startAngle = 135f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Text(
            text = "%.1f".format(value),
            style = MaterialTheme.typography.headlineMedium,
            color = Blue800
        )
    }
}

@Composable
private fun TextContent() {
    Text(
        text = "--",
        style = MaterialTheme.typography.headlineLarge,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun ButtonContent() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .background(Blue800, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.PowerSettingsNew,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Send", color = Color.White, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun SwitchContent() {
    var checked by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            if (checked) "ON" else "OFF",
            style = MaterialTheme.typography.titleLarge,
            color = if (checked) Green500 else Color.Gray
        )
        Switch(checked = checked, onCheckedChange = { checked = it })
    }
}

@Composable
private fun ChartContent() {
    Canvas(modifier = Modifier.fillMaxWidth().height(80.dp)) {
        val points = listOf(20f, 25f, 22f, 28f, 26f, 30f, 27f)
        val stepX = size.width / (points.size - 1)
        val maxY = 40f
        val minY = 0f

        for (i in 0 until points.size - 1) {
            val x1 = i * stepX
            val y1 = size.height - ((points[i] - minY) / (maxY - minY)) * size.height
            val x2 = (i + 1) * stepX
            val y2 = size.height - ((points[i + 1] - minY) / (maxY - minY)) * size.height

            drawLine(
                color = Teal400,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun LedContent() {
    val isOn = true
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isOn) Green500 else Color.Gray)
                .border(2.dp, Color.DarkGray, CircleShape)
        )
    }
}
