package com.example.mqttclient.data.repository

import com.example.mqttclient.data.model.ConnectionState
import com.example.mqttclient.data.model.MqttMessage
import com.example.mqttclient.mqtt.MqttManager
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton
import com.example.mqttclient.data.model.ConnectionConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class MqttRepository @Inject constructor(
    private val mqttManager: MqttManager,
    private val messageRepository: MessageRepository
) {
    private val _messages = MutableStateFlow<List<MqttMessage>>(emptyList())
    val messages: StateFlow<List<MqttMessage>> = _messages.asStateFlow()

    val connectionState: StateFlow<ConnectionState> = mqttManager.connectionState
    val incomingMessages: SharedFlow<MqttMessage> = mqttManager.incomingMessages
    val deliveryComplete: SharedFlow<Int> = mqttManager.deliveryComplete

    suspend fun connect(config: ConnectionConfig): Result<Unit> {
        return mqttManager.connect(config)
    }

    suspend fun disconnect(): Result<Unit> {
        return mqttManager.disconnect()
    }

    suspend fun subscribe(topic: String, qos: Int): Result<Unit> {
        return mqttManager.subscribe(topic, qos)
    }

    suspend fun unsubscribe(topic: String): Result<Unit> {
        return mqttManager.unsubscribe(topic)
    }

    suspend fun publish(topic: String, payload: ByteArray, qos: Int, retain: Boolean): Result<Int> {
        return mqttManager.publish(topic, payload, qos, retain)
    }

    fun addMessage(msg: MqttMessage) {
        _messages.value = listOf(msg) + _messages.value
    }
}
