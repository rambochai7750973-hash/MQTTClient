package com.example.mqttclient.mqtt

import com.example.mqttclient.data.model.ConnectionConfig
import com.example.mqttclient.data.model.ConnectionState
import com.example.mqttclient.data.model.MqttMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MqttManager @Inject constructor(
    private val clientFactory: MqttClientFactory
) {
    private var client: MqttClient? = null
    private var callbackHandler: MqttCallbackHandler? = null
    private val activeSubscriptions = mutableSetOf<String>()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<MqttMessage>(replay = 0, extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<MqttMessage> = _incomingMessages.asSharedFlow()

    private val _deliveryComplete = MutableSharedFlow<Int>(replay = 0)
    val deliveryComplete: SharedFlow<Int> = _deliveryComplete.asSharedFlow()

    private var currentConfig: ConnectionConfig? = null

    suspend fun connect(config: ConnectionConfig): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            _connectionState.value = ConnectionState.Connecting
            currentConfig = config

            client = clientFactory.createClient(config)
            val options = clientFactory.createConnectOptions(config)

            callbackHandler = MqttCallbackHandler(
                onConnectionLost = { cause ->
                    _connectionState.value = ConnectionState.Disconnected
                    if (config.autoReconnect) {
                        CoroutineScope(Dispatchers.IO).launch {
                            delay(1000)
                            reconnect()
                        }
                    }
                },
                onMessageArrived = { msg ->
                    CoroutineScope(Dispatchers.Default).launch {
                        _incomingMessages.emit(msg)
                    }
                },
                onDeliveryComplete = { messageId ->
                    CoroutineScope(Dispatchers.Default).launch {
                        _deliveryComplete.emit(messageId)
                    }
                },
                onConnectComplete = { reconnect, serverUri ->
                    _connectionState.value = ConnectionState.Connected(
                        serverUri = serverUri,
                        clientId = config.clientId ?: "auto",
                        connectedSince = System.currentTimeMillis()
                    )
                    if (reconnect) {
                        CoroutineScope(Dispatchers.IO).launch {
                            resubscribeAll()
                        }
                    }
                }
            )

            client?.setCallback(callbackHandler)
            client?.connect(options)

            _connectionState.value = ConnectionState.Connected(
                serverUri = config.serverUri(),
                clientId = config.clientId ?: "auto",
                connectedSince = System.currentTimeMillis()
            )
            Result.success(Unit)
        } catch (e: MqttException) {
            _connectionState.value = ConnectionState.Error(
                reason = when (e.reasonCode) {
                    MqttException.REASON_CODE_BROKER_UNAVAILABLE.toShort() -> "Broker unavailable"
                    MqttException.REASON_CODE_CLIENT_TIMEOUT.toShort() -> "Connection timeout"
                    MqttException.REASON_CODE_FAILED_AUTHENTICATION.toShort() -> "Authentication failed"
                    else -> e.message ?: "Unknown error"
                },
                throwable = e
            )
            Result.failure(e)
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error(
                reason = e.message ?: "Unknown error",
                throwable = e
            )
            Result.failure(e)
        }
    }

    suspend fun disconnect(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            _connectionState.value = ConnectionState.Disconnecting("Manual disconnect")
            client?.disconnect(5000)
            client?.close()
            client = null
            _connectionState.value = ConnectionState.Disconnected
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun subscribe(topic: String, qos: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val c = client ?: return@withContext Result.failure(Exception("Not connected"))
            c.subscribe(topic, qos)
            activeSubscriptions.add(topic)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unsubscribe(topic: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val c = client ?: return@withContext Result.failure(Exception("Not connected"))
            c.unsubscribe(topic)
            activeSubscriptions.remove(topic)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun publish(topic: String, payload: ByteArray, qos: Int, retain: Boolean): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val c = client ?: return@withContext Result.failure(Exception("Not connected"))
            val message = org.eclipse.paho.client.mqttv3.MqttMessage(payload).apply {
                this.qos = qos
                isRetained = retain
            }
            c.publish(topic, message)
            Result.success(message.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun reconnect() {
        val config = currentConfig ?: return
        try {
            connect(config)
        } catch (_: Exception) { }
    }

    private suspend fun resubscribeAll() {
        for (topic in activeSubscriptions.toList()) {
            try {
                subscribe(topic, 1)
            } catch (_: Exception) { }
        }
    }

    fun getActiveSubscriptions(): Set<String> = activeSubscriptions.toSet()
}
