package com.example.mqttclient.ui.screen.messages

import com.example.mqttclient.data.model.ConnectionState
import com.example.mqttclient.data.model.MqttMessage
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

data class MessagesUiState(
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val messages: List<MqttMessage> = emptyList(),
    val searchQuery: String = "",
    val selectedMessage: MqttMessage? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val mqttRepository: MqttRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            mqttRepository.connectionState.collect { state ->
                _uiState.update { it.copy(connectionState = state) }
            }
        }
        viewModelScope.launch {
            mqttRepository.messages.collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
        viewModelScope.launch {
            mqttRepository.incomingMessages.collect { msg ->
                mqttRepository.addMessage(msg)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onMessageClick(message: MqttMessage) {
        _uiState.update { it.copy(selectedMessage = message) }
    }

    fun onDismissSheet() {
        _uiState.update { it.copy(selectedMessage = null) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(messages = emptyList()) }
    }
}
