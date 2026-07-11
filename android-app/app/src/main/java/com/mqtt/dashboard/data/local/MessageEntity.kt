package com.mqtt.dashboard.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val connectionId: Long,
    val topic: String,
    val payload: String,
    val qos: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val direction: String = "inbound"
)
