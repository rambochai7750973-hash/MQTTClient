package com.mqtt.dashboard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Blue800,
    onPrimary = White,
    primaryContainer = Blue800,
    secondary = Teal400,
    background = Grey50,
    surface = White,
    onSurface = Grey900,
    onBackground = Grey900,
    error = Red500
)

@Composable
fun MqttDashboardTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
