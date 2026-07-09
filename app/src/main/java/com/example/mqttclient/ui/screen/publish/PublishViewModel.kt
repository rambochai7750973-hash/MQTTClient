package com.example.mqttclient.ui.screen.publish

import com.example.mqttclient.data.model.ConnectionState
import com.example.mqttclient.data.repository.MessageRepository
import com.example.mqttclient.data.repository.MqttRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

data class PublishUiState(
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val topic: String = "",
    val payload: String = "",
    val qos: Int = 1,
    val retain: Boolean = false,
    val recentTopics: List<String> = emptyList(),
    val isPayloadHex: Boolean = false,
    val publishSuccess: Boolean? = null,
    val publishError: String? = null
)

@HiltViewModel
class PublishViewModel @Inject constructor(
    private val mqttRepository: MqttRepository,
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublishUiState())
    val uiState: StateFlow<PublishUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            mqttRepository.connectionState.collect { state ->
                _uiState.update { it.copy(connectionState = state) }
            }
        }
        viewModelScope.launch {
            messageRepository.getRecentPublishTopics().collect { topics ->
                _uiState.update { it.copy(recentTopics = topics) }
            }
        }
    }

    fun onTopicChanged(topic: String) {
        _uiState.update { it.copy(topic = topic) }
    }

    fun onPayloadChanged(payload: String) {
        _uiState.update { it.copy(payload = payload) }
    }

    fun onQosChanged(qos: Int) {
        _uiState.update { it.copy(qos = qos) }
    }

    fun onRetainChanged(retain: Boolean) {
        _uiState.update { it.copy(retain = retain) }
    }

    fun onPayloadModeChanged(isHex: Boolean) {
        _uiState.update { it.copy(isPayloadHex = isHex) }
    }

    fun publish() {
        val state = _uiState.value
        val topic = state.topic.trim()
        if (topic.isBlank()) return

        val payloadBytes = if (state.isPayloadHex) {
            try {
                state.payload.replace(" ", "").chunked(2).map { it.toInt(16).toByte() }.toByteArray()
            } catch (_: Exception) {
                state.payload.toByteArray()
            }
        } else {
            state.payload.toByteArray()
        }

        viewModelScope.launch {
            val result = mqttRepository.publish(topic, payloadBytes, state.qos, state.retain)
            _uiState.update {
                it.copy(
                    publishSuccess = result.isSuccess,
                    publishError = result.exceptionOrNull()?.message,
                    payload = if (result.isSuccess) "" else it.payload
                )
            }
            if (result.isSuccess) {
                messageRepository.savePublishTopic(topic)
            }
        }
    }

    fun clearResult() {
        _uiState.update { it.copy(publishSuccess = null, publishError = null) }
    }
}
