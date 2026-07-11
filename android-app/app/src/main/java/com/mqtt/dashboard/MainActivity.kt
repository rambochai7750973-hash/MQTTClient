package com.mqtt.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.mqtt.dashboard.data.repository.MqttRepository
import com.mqtt.dashboard.ui.navigation.MqttNavGraph
import com.mqtt.dashboard.ui.theme.MqttDashboardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as MqttDashboardApp
        val repository = MqttRepository(app.database, app.mqttManager)

        repository.startListeningForMessages()

        setContent {
            MqttDashboardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    MqttNavGraph(
                        navController = navController,
                        repository = repository
                    )
                }
            }
        }
    }
}
