package com.example.mqttclient.ui.screen.configs

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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

data class SavedConfigsUiState(
    val configs: List<ConnectionConfig> = emptyList(),
    val showDeleteConfirm: ConnectionConfig? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class SavedConfigsViewModel @Inject constructor(
    private val connectionRepository: ConnectionRepository,
    private val mqttRepository: MqttRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedConfigsUiState())
    val uiState: StateFlow<SavedConfigsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            connectionRepository.getAll().collect { configs ->
                _uiState.update { it.copy(configs = configs) }
            }
        }
    }

    fun connect(config: ConnectionConfig) {
        viewModelScope.launch {
            mqttRepository.connect(config)
        }
    }

    fun deleteConfig(config: ConnectionConfig) {
        viewModelScope.launch {
            connectionRepository.delete(config)
        }
    }

    fun showDeleteConfirm(config: ConnectionConfig) {
        _uiState.update { it.copy(showDeleteConfirm = config) }
    }

    fun dismissDeleteConfirm() {
        _uiState.update { it.copy(showDeleteConfirm = null) }
    }
}
