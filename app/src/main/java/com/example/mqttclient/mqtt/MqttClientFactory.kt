package com.example.mqttclient.mqtt

import android.content.Context
import com.example.mqttclient.data.model.ConnectionConfig
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MqttClientFactory @Inject constructor() {

    fun createClient(context: Context, config: ConnectionConfig): MqttAndroidClient {
        return MqttAndroidClient(context, config.serverUri(), config.clientId ?: generateClientId())
    }

    fun createConnectOptions(config: ConnectionConfig): MqttConnectOptions {
        return MqttConnectOptions().apply {
            isCleanSession = config.cleanSession
            userName = config.username
            if (config.password != null) {
                this.password = config.password.toCharArray()
            }
            connectionTimeout = config.connectTimeoutSec
            keepAliveInterval = config.keepAliveSec
            isAutomaticReconnect = config.autoReconnect
            maxReconnectDelay = config.reconnectIntervalSec * 1000

            if (config.willTopic != null) {
                willDestination = config.willTopic
                willMessage = (config.willPayload ?: "").toByteArray()
                willQos = config.willQos
                isWillRetain = config.willRetain
            }
        }
    }

    private fun generateClientId(): String {
        return "mqtt_${java.util.UUID.randomUUID().toString().take(8)}"
    }
}
