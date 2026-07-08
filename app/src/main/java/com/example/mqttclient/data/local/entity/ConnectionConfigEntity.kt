package com.example.mqttclient.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "connection_config")
data class ConnectionConfigEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val protocol: String = "tcp",
    val host: String,
    val port: Int = 1883,
    val clientId: String? = null,
    val username: String? = null,
    val password: String? = null,
    val cleanSession: Boolean = true,
    val autoReconnect: Boolean = true,
    val reconnectIntervalSec: Int = 5,
    val connectTimeoutSec: Int = 10,
    val keepAliveSec: Int = 60,
    val sessionExpirySec: Int = 0,
    val willTopic: String? = null,
    val willPayload: String? = null,
    val willQos: Int = 0,
    val willRetain: Boolean = false,
    val tlsEnabled: Boolean = false,
    val tlsTrustAll: Boolean = true,
    val wsPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
