package com.example.mqttclient.ui.screen.subscriptions

import com.example.mqttclient.data.model.ConnectionState
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

data class SubscriptionsUiState(
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val subscriptions: List<SubscriptionItem> = emptyList(),
    val newTopic: String = "",
    val newQos: Int = 1,
    val isLoading: Boolean = false
)

data class SubscriptionItem(
    val topic: String,
    val qos: Int,
    val subscribedAt: Long = System.currentTimeMillis()
)

@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val mqttRepository: MqttRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionsUiState())
    val uiState: StateFlow<SubscriptionsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            mqttRepository.connectionState.collect { state ->
                _uiState.update { it.copy(connectionState = state) }
            }
        }
    }

    fun onTopicChanged(topic: String) {
        _uiState.update { it.copy(newTopic = topic) }
    }

    fun onQosChanged(qos: Int) {
        _uiState.update { it.copy(newQos = qos) }
    }

    fun subscribe() {
        val topic = _uiState.value.newTopic.trim()
        val qos = _uiState.value.newQos
        if (topic.isBlank() || topic in _uiState.value.subscriptions.map { it.topic }) return

        viewModelScope.launch {
            val result = mqttRepository.subscribe(topic, qos)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        subscriptions = it.subscriptions + SubscriptionItem(topic, qos),
                        newTopic = ""
                    )
                }
            }
        }
    }

    fun unsubscribe(topic: String) {
        viewModelScope.launch {
            val result = mqttRepository.unsubscribe(topic)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(subscriptions = it.subscriptions.filter { s -> s.topic != topic })
                }
            }
        }
    }
}
