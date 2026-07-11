package com.mqtt.dashboard.data.mqtt

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage

enum class ConnectionState {
    DISCONNECTED, CONNECTING, CONNECTED, ERROR
}

data class MqttMessageEvent(
    val topic: String,
    val payload: String,
    val qos: Int,
    val timestamp: Long = System.currentTimeMillis()
)

class MqttManager(private val context: Context) {

    private var client: MqttAndroidClient? = null

    private val _connectionState = MutableLiveData(ConnectionState.DISCONNECTED)
    val connectionState: LiveData<ConnectionState> = _connectionState

    private val _messageEvents = MutableLiveData<MqttMessageEvent>()
    val messageEvents: LiveData<MqttMessageEvent> = _messageEvents

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val callback = object : MqttCallback {
        override fun connectionLost(cause: Throwable?) {
            _connectionState.postValue(ConnectionState.DISCONNECTED)
            _errorMessage.postValue("Connection lost: ${cause?.message}")
        }

        override fun messageArrived(topic: String?, message: MqttMessage?) {
            if (topic != null && message != null) {
                _messageEvents.postValue(
                    MqttMessageEvent(
                        topic = topic,
                        payload = String(message.payload),
                        qos = message.qos
                    )
                )
            }
        }

        override fun deliveryComplete(token: IMqttDeliveryToken?) {}
    }

    fun connect(config: MqttConnectionConfig) {
        disconnect()

        val serverURI = buildServerUri(config)
        _connectionState.postValue(ConnectionState.CONNECTING)

        try {
            client = MqttAndroidClient(context, serverURI, config.clientId)
            client?.setCallback(callback)

            val options = MqttConnectOptions().apply {
                userName = config.username.ifBlank { null }
                password = config.password.ifBlank { null }?.toCharArray()
                cleanSession = config.cleanSession
                connectionTimeout = config.connectionTimeout
                keepAliveInterval = config.keepAliveInterval
                if (config.authToken.isNotBlank()) {
                    setCustomHeader("Authorization", "Bearer ${config.authToken}")
                }
            }

            client?.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    _connectionState.postValue(ConnectionState.CONNECTED)
                    _errorMessage.postValue(null)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    _connectionState.postValue(ConnectionState.ERROR)
                    _errorMessage.postValue("Connect failed: ${exception?.message}")
                }
            })
        } catch (e: MqttException) {
            _connectionState.postValue(ConnectionState.ERROR)
            _errorMessage.postValue("MQTT error: ${e.message}")
        }
    }

    fun disconnect() {
        try {
            client?.unregisterResources()
            client?.disconnect()
        } catch (_: Exception) {}
        client = null
        _connectionState.postValue(ConnectionState.DISCONNECTED)
    }

    fun subscribe(topic: String, qos: Int = 0) {
        client?.subscribe(topic, qos, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                _errorMessage.postValue(null)
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                _errorMessage.postValue("Subscribe failed: ${exception?.message}")
            }
        })
    }

    fun unsubscribe(topic: String) {
        client?.unsubscribe(topic)
    }

    fun publish(topic: String, payload: String, qos: Int = 0, retained: Boolean = false) {
        val message = MqttMessage(payload.toByteArray()).apply {
            this.qos = qos
            isRetained = retained
        }
        client?.publish(topic, message, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                _messageEvents.postValue(
                    MqttMessageEvent(
                        topic = topic,
                        payload = payload,
                        qos = qos,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                _errorMessage.postValue("Publish failed: ${exception?.message}")
            }
        })
    }

    fun isConnected(): Boolean = client?.isConnected == true

    private fun buildServerUri(config: MqttConnectionConfig): String {
        val host = config.workerHost
        val port = config.workerPort
        val path = config.workerPath.let {
            if (it.startsWith("/")) it else "/$it"
        }

        val queryParams = mutableListOf<String>()
        if (config.brokerHost.isNotBlank()) {
            queryParams.add("host=${config.brokerHost}")
        }
        if (config.brokerPort.isNotBlank()) {
            queryParams.add("port=${config.brokerPort}")
        }
        if (!config.useTls) {
            queryParams.add("tls=false")
        }

        val query = if (queryParams.isNotEmpty()) "?${queryParams.joinToString("&")}" else ""
        return "wss://$host:$port$path$query"
    }
}

data class MqttConnectionConfig(
    val workerHost: String,
    val workerPort: Int = 443,
    val workerPath: String = "/mqtt",
    val brokerHost: String = "",
    val brokerPort: String = "",
    val useTls: Boolean = true,
    val username: String = "",
    val password: String = "",
    val clientId: String = "android_" + System.currentTimeMillis().toString(16),
    val authToken: String = "",
    val cleanSession: Boolean = true,
    val connectionTimeout: Int = 10,
    val keepAliveInterval: Int = 20
)
