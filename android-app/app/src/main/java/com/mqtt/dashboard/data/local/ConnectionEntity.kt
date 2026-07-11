package com.mqtt.dashboard.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "connections")
data class ConnectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
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
    val keepAliveInterval: Int = 20,
    val createdAt: Long = System.currentTimeMillis()
)
