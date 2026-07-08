package com.example.mqttclient.mqtt

import com.example.mqttclient.data.model.MqttMessage
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage as PahoMessage

class MqttCallbackHandler(
    private val onConnectionLost: (Throwable?) -> Unit,
    private val onMessageArrived: (MqttMessage) -> Unit,
    private val onDeliveryComplete: (Int) -> Unit,
    private val onConnectComplete: (Boolean, String) -> Unit
) : MqttCallbackExtended {

    override fun connectComplete(reconnect: Boolean, serverURI: String) {
        onConnectComplete(reconnect, serverURI)
    }

    override fun connectionLost(cause: Throwable?) {
        onConnectionLost(cause)
    }

    override fun messageArrived(topic: String, message: PahoMessage) {
        val msg = MqttMessage(
            topic = topic,
            payload = message.payload,
            qos = message.qos,
            retained = message.isRetained,
            isIncoming = true,
            timestamp = System.currentTimeMillis()
        )
        onMessageArrived(msg)
    }

    override fun deliveryComplete(token: IMqttDeliveryToken) {
        onDeliveryComplete(token.messageId)
    }
}
