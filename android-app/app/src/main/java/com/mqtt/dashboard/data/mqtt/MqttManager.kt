package com.mqtt.dashboard.data.mqtt

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import info.mqtt.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage

private const val TAG = "MqttManager"

enum class ConnectionState {
    DISCONNECTED, CONNECTING, CONNECTED, ERROR
}

data class MqttMessageEvent(
    val topic: String,
    val payload: String,
    val qos: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class ConnectionLogEntry(
    val message: String,
    val level: String,
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

    private val _connectionLog = MutableLiveData<List<ConnectionLogEntry>>(emptyList())
    val connectionLog: LiveData<List<ConnectionLogEntry>> = _connectionLog

    private fun addLog(level: String, message: String) {
        Log.d(TAG, "[$level] $message")
        val current = _connectionLog.value.orEmpty().toMutableList()
        current.add(ConnectionLogEntry(message = message, level = level))
        if (current.size > 200) current.removeAt(0)
        _connectionLog.postValue(current)
    }

    private fun addInfoLog(msg: String) = addLog("I", msg)
    private fun addErrorLog(msg: String) = addLog("E", msg)
    private fun addWarnLog(msg: String) = addLog("W", msg)

    private val callback = object : MqttCallback {
        override fun connectionLost(cause: Throwable?) {
            try {
                addWarnLog("连接丢失: ${cause?.message}")
                _connectionState.postValue(ConnectionState.DISCONNECTED)
                _errorMessage.postValue("连接丢失：${cause?.message}")
            } catch (e: Exception) {
                addErrorLog("connectionLost 处理异常: ${e.message}")
                Log.e(TAG, "connectionLost", e)
            }
        }

        override fun messageArrived(topic: String?, message: MqttMessage?) {
            try {
                if (topic != null && message != null) {
                    val payload = String(message.payload)
                    addInfoLog("收到消息: $topic -> $payload")
                    _messageEvents.postValue(
                        MqttMessageEvent(
                            topic = topic,
                            payload = payload,
                            qos = message.qos
                        )
                    )
                }
            } catch (e: Exception) {
                addErrorLog("messageArrived 处理异常: ${e.message}")
                Log.e(TAG, "messageArrived", e)
            }
        }

        override fun deliveryComplete(token: IMqttDeliveryToken?) {
            try {
                addInfoLog("消息已送达")
            } catch (e: Exception) {
                addWarnLog("deliveryComplete 处理异常: ${e.message}")
                Log.e(TAG, "deliveryComplete", e)
            }
        }
    }

    fun connect(config: MqttConnectionConfig) {
        addInfoLog("=== 开始连接 ===")
        addInfoLog("主机: ${config.workerHost}:${config.workerPort}${config.workerPath}")
        addInfoLog("客户端ID: ${config.clientId}")
        addInfoLog("代理: ${config.brokerHost}:${config.brokerPort}")
        addInfoLog("TLS: ${config.useTls}")

        try {
            disconnect()

            val serverURI = buildServerUri(config)
            addInfoLog("完整URI: $serverURI")

            _connectionState.postValue(ConnectionState.CONNECTING)
            _errorMessage.postValue(null)

            addInfoLog("创建 MqttAndroidClient...")
            try {
                client = MqttAndroidClient(context, serverURI, config.clientId)
                client?.setCallback(callback)
                addInfoLog("MqttAndroidClient 创建成功")
            } catch (e: Exception) {
                addErrorLog("创建 MqttAndroidClient 异常: ${e.message}")
                Log.e(TAG, "create client", e)
                _connectionState.postValue(ConnectionState.ERROR)
                _errorMessage.postValue("创建客户端异常：${e.message}")
                return
            }

            val options = MqttConnectOptions().apply {
                userName = config.username.ifBlank { null }
                password = config.password.ifBlank { null }?.toCharArray()
                setCleanSession(config.cleanSession)
                connectionTimeout = config.connectionTimeout
                keepAliveInterval = config.keepAliveInterval
                isAutomaticReconnect = true
            }

            addInfoLog("正在连接...")
            try {
                client?.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    addInfoLog("连接成功! token=${asyncActionToken}")
                    _connectionState.postValue(ConnectionState.CONNECTED)
                    _errorMessage.postValue(null)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    val exType = exception?.javaClass?.simpleName ?: "未知"
                    val exMsg = exception?.message ?: "无详细信息"
                    addErrorLog("连接失败 [$exType]: $exMsg")
                    if (exception is MqttException) {
                        addErrorLog("MqttException reasonCode=${exception.reasonCode}")
                    }
                    _connectionState.postValue(ConnectionState.ERROR)
                    _errorMessage.postValue("连接失败：$exMsg")
                }
            })
            } catch (e: Exception) {
                addErrorLog("connect 调用抛出异常: ${e.message}")
                Log.e(TAG, "connect", e)
                _connectionState.postValue(ConnectionState.ERROR)
                _errorMessage.postValue("连接调用异常：${e.message}")
            }
        } catch (e: MqttException) {
            addErrorLog("MqttException: reasonCode=${e.reasonCode}, msg=${e.message}")
            _connectionState.postValue(ConnectionState.ERROR)
            _errorMessage.postValue("MQTT异常 (${e.reasonCode})：${e.message}")
        } catch (e: IllegalArgumentException) {
            addErrorLog("参数错误: ${e.message}")
            _connectionState.postValue(ConnectionState.ERROR)
            _errorMessage.postValue("参数错误：${e.message}")
        } catch (e: Exception) {
            val exType = e.javaClass.simpleName
            addErrorLog("异常 [$exType]: ${e.message}")
            _connectionState.postValue(ConnectionState.ERROR)
            _errorMessage.postValue("异常 [$exType]：${e.message}")
        }
    }

    fun disconnect() {
        try {
            if (client?.isConnected == true) {
                addInfoLog("主动断开连接")
                client?.disconnect(0)
            }
            client?.unregisterResources()
        } catch (e: Exception) {
            addWarnLog("断开时异常: ${e.message}")
        }
        client = null
        _connectionState.postValue(ConnectionState.DISCONNECTED)
    }

    fun subscribe(topic: String, qos: Int = 0) {
        if (!isConnected()) {
            addWarnLog("未连接，无法订阅")
            _errorMessage.postValue("未连接，无法订阅")
            return
        }
        addInfoLog("订阅主题: $topic (QoS=$qos)")
        try {
            client?.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    addInfoLog("订阅成功: $topic")
                    _errorMessage.postValue(null)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    addErrorLog("订阅失败: ${exception?.message}")
                    _errorMessage.postValue("订阅失败：${exception?.message}")
                }
            })
        } catch (e: Exception) {
            addErrorLog("订阅异常: ${e.message}")
            _errorMessage.postValue("订阅异常：${e.message}")
        }
    }

    fun unsubscribe(topic: String) {
        if (!isConnected()) return
        addInfoLog("取消订阅: $topic")
        try {
            client?.unsubscribe(topic)
        } catch (e: Exception) {
            addErrorLog("取消订阅异常: ${e.message}")
        }
    }

    fun publish(topic: String, payload: String, qos: Int = 0, retained: Boolean = false) {
        if (!isConnected()) {
            addWarnLog("未连接，无法发布")
            _errorMessage.postValue("未连接，无法发布")
            return
        }
        addInfoLog("发布消息: $topic -> $payload (QoS=$qos, retain=$retained)")
        val message = MqttMessage(payload.toByteArray()).apply {
            this.qos = qos
            isRetained = retained
        }
        try {
            client?.publish(topic, message, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    addInfoLog("发布成功: $topic")
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
                    addErrorLog("发布失败: ${exception?.message}")
                    _errorMessage.postValue("发布失败：${exception?.message}")
                }
            })
        } catch (e: Exception) {
            addErrorLog("发布异常: ${e.message}")
            _errorMessage.postValue("发布异常：${e.message}")
        }
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
        if (config.authToken.isNotBlank()) {
            queryParams.add("token=${config.authToken}")
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
