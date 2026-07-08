package com.example.mqttclient.data.model

data class MqttMessage(
    val id: Long = 0,
    val topic: String,
    val payload: ByteArray,
    val qos: Int,
    val retained: Boolean,
    val isIncoming: Boolean,
    val timestamp: Long,
    val isDelivered: Boolean? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MqttMessage) return false
        return id == other.id && topic == other.topic &&
                payload.contentEquals(other.payload) && qos == other.qos &&
                retained == other.retained && isIncoming == other.isIncoming &&
                timestamp == other.timestamp
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + topic.hashCode()
        result = 31 * result + payload.contentHashCode()
        result = 31 * result + qos
        return result
    }

    fun payloadText(): String = String(payload, Charsets.UTF_8)
}
