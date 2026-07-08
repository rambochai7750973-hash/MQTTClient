package com.example.mqttclient.data.model

sealed interface ConnectionState {
    data object Disconnected : ConnectionState
    data object Connecting : ConnectionState
    data class Connected(
        val serverUri: String,
        val clientId: String,
        val connectedSince: Long = System.currentTimeMillis()
    ) : ConnectionState
    data class Disconnecting(val reason: String? = null) : ConnectionState
    data class Error(val reason: String, val throwable: Throwable? = null) : ConnectionState
}
