package com.example.mqttclient.ui.screen.connection

import com.example.mqttclient.data.model.ConnectionConfig
import com.example.mqttclient.data.repository.ConnectionRepository
import com.example.mqttclient.data.repository.MqttRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

data class ConnectionSettingsUiState(
    val configId: Long = -1L,
    val name: String = "",
    val protocol: String = "tcp",
    val host: String = "",
    val port: String = "1883",
    val clientId: String = "",
    val username: String = "",
    val password: String = "",
    val cleanSession: Boolean = true,
    val autoReconnect: Boolean = true,
    val reconnectIntervalSec: Int = 5,
    val connectTimeoutSec: Int = 10,
    val keepAliveSec: Int = 60,
    val sessionExpirySec: Int = 0,
    val willEnabled: Boolean = false,
    val willTopic: String = "",
    val willPayload: String = "",
    val willQos: Int = 0,
    val willRetain: Boolean = false,
    val tlsEnabled: Boolean = false,
    val tlsTrustAll: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val connectResult: String? = null
)

@HiltViewModel
class ConnectionSettingsViewModel @Inject constructor(
    private val connectionRepository: ConnectionRepository,
    private val mqttRepository: MqttRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val configId: Long = savedStateHandle.get<Long>("configId") ?: -1L
    private val _uiState = MutableStateFlow(ConnectionSettingsUiState(configId = configId))
    val uiState: StateFlow<ConnectionSettingsUiState> = _uiState.asStateFlow()

    init {
        if (configId > 0) {
            loadConfig(configId)
        } else {
            generateClientId()
        }
    }

    private fun loadConfig(id: Long) {
        viewModelScope.launch {
            val config = connectionRepository.getById(id)
            if (config != null) {
                _uiState.update {
                    it.copy(
                        name = config.name,
                        protocol = config.protocol,
                        host = config.host,
                        port = config.port.toString(),
                        clientId = config.clientId ?: "",
                        username = config.username ?: "",
                        password = config.password ?: "",
                        cleanSession = config.cleanSession,
                        autoReconnect = config.autoReconnect,
                        reconnectIntervalSec = config.reconnectIntervalSec,
                        connectTimeoutSec = config.connectTimeoutSec,
                        keepAliveSec = config.keepAliveSec,
                        sessionExpirySec = config.sessionExpirySec,
                        willEnabled = config.willTopic != null,
                        willTopic = config.willTopic ?: "",
                        willPayload = config.willPayload ?: "",
                        willQos = config.willQos,
                        willRetain = config.willRetain,
                        tlsEnabled = config.tlsEnabled,
                        tlsTrustAll = config.tlsTrustAll
                    )
                }
            }
        }
    }

    fun generateClientId() {
        _uiState.update { it.copy(clientId = "mqtt_${java.util.UUID.randomUUID().toString().take(8)}") }
    }

    fun onNameChanged(name: String) { _uiState.update { it.copy(name = name) } }
    fun onProtocolChanged(protocol: String) { _uiState.update { it.copy(protocol = protocol) } }
    fun onHostChanged(host: String) { _uiState.update { it.copy(host = host) } }
    fun onPortChanged(port: String) { _uiState.update { it.copy(port = port) } }
    fun onClientIdChanged(clientId: String) { _uiState.update { it.copy(clientId = clientId) } }
    fun onUsernameChanged(username: String) { _uiState.update { it.copy(username = username) } }
    fun onPasswordChanged(password: String) { _uiState.update { it.copy(password = password) } }
    fun onCleanSessionChanged(v: Boolean) { _uiState.update { it.copy(cleanSession = v) } }
    fun onAutoReconnectChanged(v: Boolean) { _uiState.update { it.copy(autoReconnect = v) } }
    fun onWillEnabledChanged(v: Boolean) { _uiState.update { it.copy(willEnabled = v) } }
    fun onWillTopicChanged(v: String) { _uiState.update { it.copy(willTopic = v) } }
    fun onWillPayloadChanged(v: String) { _uiState.update { it.copy(willPayload = v) } }
    fun onWillQosChanged(v: Int) { _uiState.update { it.copy(willQos = v) } }
    fun onWillRetainChanged(v: Boolean) { _uiState.update { it.copy(willRetain = v) } }
    fun onTlsEnabledChanged(v: Boolean) { _uiState.update { it.copy(tlsEnabled = v) } }
    fun onTlsTrustAllChanged(v: Boolean) { _uiState.update { it.copy(tlsTrustAll = v) } }

    fun saveAndConnect() {
        val state = _uiState.value
        val port = state.port.toIntOrNull() ?: 1883
        if (state.host.isBlank() || state.name.isBlank()) {
            _uiState.update { it.copy(error = "Name and host are required") }
            return
        }

        val config = ConnectionConfig(
            id = state.configId.takeIf { it > 0 } ?: 0,
            name = state.name,
            protocol = state.protocol,
            host = state.host,
            port = port,
            clientId = state.clientId.ifBlank { null },
            username = state.username.ifBlank { null },
            password = state.password.ifBlank { null },
            cleanSession = state.cleanSession,
            autoReconnect = state.autoReconnect,
            reconnectIntervalSec = state.reconnectIntervalSec,
            connectTimeoutSec = state.connectTimeoutSec,
            keepAliveSec = state.keepAliveSec,
            sessionExpirySec = state.sessionExpirySec,
            willTopic = state.willTopic.ifBlank { null },
            willPayload = state.willPayload.ifBlank { null },
            willQos = state.willQos,
            willRetain = state.willRetain,
            tlsEnabled = state.tlsEnabled,
            tlsTrustAll = state.tlsTrustAll
        )

        viewModelScope.launch {
            val id = connectionRepository.save(config)
            _uiState.update { it.copy(configId = id) }

            val result = mqttRepository.connect(config)
            _uiState.update {
                it.copy(
                    connectResult = if (result.isSuccess) "Connected ✓"
                    else "Failed: ${result.exceptionOrNull()?.message}",
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun clearResult() { _uiState.update { it.copy(connectResult = null, error = null) } }
}
