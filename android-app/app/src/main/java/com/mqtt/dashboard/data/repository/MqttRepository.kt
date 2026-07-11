package com.mqtt.dashboard.data.repository

import com.mqtt.dashboard.data.local.AppDatabase
import com.mqtt.dashboard.data.local.ConnectionEntity
import com.mqtt.dashboard.data.local.MessageEntity
import com.mqtt.dashboard.data.local.WidgetEntity
import com.mqtt.dashboard.data.mqtt.MqttManager
import com.mqtt.dashboard.data.mqtt.MqttMessageEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MqttRepository(
    val database: AppDatabase,
    private val mqttManager: MqttManager
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun startListeningForMessages() {
        mqttManager.messageEvents.observeForever { event ->
            if (event != null) {
                scope.launch {
                    database.messageDao().insert(
                        MessageEntity(
                            connectionId = currentConnectionId,
                            topic = event.topic,
                            payload = event.payload,
                            qos = event.qos,
                            timestamp = event.timestamp,
                            direction = determineDirection(event)
                        )
                    )
                }
            }
        }
    }

    private var currentConnectionId: Long = 0

    fun setCurrentConnectionId(id: Long) {
        currentConnectionId = id
    }

    private fun determineDirection(event: MqttMessageEvent): String {
        return "inbound"
    }

    fun getConnections(): Flow<List<ConnectionEntity>> {
        return database.connectionDao().getAllConnections()
    }

    suspend fun saveConnection(connection: ConnectionEntity): Long {
        return database.connectionDao().insert(connection)
    }

    suspend fun updateConnection(connection: ConnectionEntity) {
        database.connectionDao().update(connection)
    }

    suspend fun deleteConnection(connection: ConnectionEntity) {
        database.connectionDao().delete(connection)
    }

    fun getMessages(connectionId: Long): Flow<List<MessageEntity>> {
        return database.messageDao().getMessages(connectionId)
    }

    suspend fun saveMessage(message: MessageEntity): Long {
        return database.messageDao().insert(message)
    }

    suspend fun clearMessages(connectionId: Long) {
        database.messageDao().deleteByConnectionId(connectionId)
    }

    fun getWidgets(connectionId: Long): Flow<List<WidgetEntity>> {
        return database.widgetDao().getWidgets(connectionId)
    }

    suspend fun saveWidget(widget: WidgetEntity): Long {
        return database.widgetDao().insert(widget)
    }

    suspend fun deleteWidget(widget: WidgetEntity) {
        database.widgetDao().delete(widget)
    }
}
