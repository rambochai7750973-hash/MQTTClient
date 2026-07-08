package com.example.mqttclient.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Arrays

@Entity(
    tableName = "message_history",
    indices = [
        Index("topic"),
        Index("timestamp")
    ]
)
data class MessageHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topic: String,
    val payload: ByteArray,
    val qos: Int,
    val retained: Boolean = false,
    val isIncoming: Boolean = true,
    val isDelivered: Boolean? = null,
    val timestamp: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MessageHistoryEntity) return false
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
}
