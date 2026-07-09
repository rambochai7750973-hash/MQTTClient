package com.example.mqttclient.mqtt

import com.example.mqttclient.data.model.ConnectionConfig
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MqttClientFactory @Inject constructor() {

    fun createClient(config: ConnectionConfig): MqttClient {
        val clientId = config.clientId ?: generateClientId()
        return MqttClient(config.serverUri(), clientId, null)
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
                val willMessage = org.eclipse.paho.client.mqttv3.MqttMessage()
                willMessage.payload = (config.willPayload ?: "").toByteArray()
                willMessage.qos = config.willQos
                willMessage.isRetained = config.willRetain
                setWill(config.willTopic, willMessage)
            }
        }
    }

    private fun generateClientId(): String {
        return "mqtt_${java.util.UUID.randomUUID().toString().take(8)}"
    }
}
