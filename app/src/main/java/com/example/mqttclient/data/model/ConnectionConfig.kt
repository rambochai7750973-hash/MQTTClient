package com.example.mqttclient.data.model

import com.example.mqttclient.data.local.entity.ConnectionConfigEntity

data class ConnectionConfig(
    val id: Long = 0,
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
) {
    fun serverUri(): String {
        val auth = if (username != null && password != null) {
            "$username:$password@"
        } else ""

        return when {
            protocol == "ws" || protocol == "wss" -> "$protocol://$auth$host:$port${wsPath ?: ""}"
            else -> "$protocol://$auth$host:$port"
        }
    }

    fun toEntity(): ConnectionConfigEntity {
        return ConnectionConfigEntity(
            id = id,
            name = name,
            protocol = protocol,
            host = host,
            port = port,
            clientId = clientId,
            username = username,
            password = password,
            cleanSession = cleanSession,
            autoReconnect = autoReconnect,
            reconnectIntervalSec = reconnectIntervalSec,
            connectTimeoutSec = connectTimeoutSec,
            keepAliveSec = keepAliveSec,
            sessionExpirySec = sessionExpirySec,
            willTopic = willTopic,
            willPayload = willPayload,
            willQos = willQos,
            willRetain = willRetain,
            tlsEnabled = tlsEnabled,
            tlsTrustAll = tlsTrustAll,
            wsPath = wsPath,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromEntity(entity: ConnectionConfigEntity): ConnectionConfig {
            return ConnectionConfig(
                id = entity.id,
                name = entity.name,
                protocol = entity.protocol,
                host = entity.host,
                port = entity.port,
                clientId = entity.clientId,
                username = entity.username,
                password = entity.password,
                cleanSession = entity.cleanSession,
                autoReconnect = entity.autoReconnect,
                reconnectIntervalSec = entity.reconnectIntervalSec,
                connectTimeoutSec = entity.connectTimeoutSec,
                keepAliveSec = entity.keepAliveSec,
                sessionExpirySec = entity.sessionExpirySec,
                willTopic = entity.willTopic,
                willPayload = entity.willPayload,
                willQos = entity.willQos,
                willRetain = entity.willRetain,
                tlsEnabled = entity.tlsEnabled,
                tlsTrustAll = entity.tlsTrustAll,
                wsPath = entity.wsPath,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt
            )
        }
    }
}
