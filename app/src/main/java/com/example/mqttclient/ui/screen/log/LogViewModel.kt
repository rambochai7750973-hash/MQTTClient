package com.example.mqttclient.ui.screen.log

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class LogEntry(
    val timestamp: Long,
    val level: LogLevel,
    val message: String
)

enum class LogLevel { DEBUG, INFO, WARN, ERROR }

data class LogUiState(
    val logs: List<LogEntry> = emptyList()
)

@HiltViewModel
class LogViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LogUiState())
    val uiState: StateFlow<LogUiState> = _uiState.asStateFlow()

    // Log capture would be implemented here
    // For now, provide example logs
    init {
        addLog(LogLevel.INFO, "MQTT Client started")
        addLog(LogLevel.DEBUG, "Ready to connect")
    }

    fun addLog(level: LogLevel, message: String) {
        val entry = LogEntry(
            timestamp = System.currentTimeMillis(),
            level = level,
            message = message
        )
        _uiState.value = _uiState.value.copy(
            logs = _uiState.value.logs + entry
        )
    }

    fun clearLogs() {
        _uiState.value = LogUiState()
    }
}
