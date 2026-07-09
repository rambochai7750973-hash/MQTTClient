package com.example.mqttclient.ui.screen.log

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mqttclient.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
class LogViewModel @Inject constructor(
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogUiState())
    val uiState: StateFlow<LogUiState> = _uiState.asStateFlow()

    init {
        addLog(LogLevel.INFO, application.getString(R.string.log_started))
        addLog(LogLevel.DEBUG, application.getString(R.string.log_ready))
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
