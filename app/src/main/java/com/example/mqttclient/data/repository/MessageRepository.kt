package com.example.mqttclient.data.repository

import com.example.mqttclient.data.local.dao.MessageHistoryDao
import com.example.mqttclient.data.local.dao.PublishHistoryDao
import com.example.mqttclient.data.local.entity.MessageHistoryEntity
import com.example.mqttclient.data.local.entity.PublishHistoryEntity
import com.example.mqttclient.data.model.MqttMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val messageDao: MessageHistoryDao,
    private val publishDao: PublishHistoryDao
) {
    fun getMessagesPaged(limit: Int = 50, offset: Int = 0): Flow<List<MqttMessage>> {
        return messageDao.getMessagesPaged(limit, offset).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun searchMessages(query: String): Flow<List<MqttMessage>> {
        return messageDao.searchMessages(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getRecentPublishTopics(): Flow<List<String>> {
        return publishDao.getRecentTopics()
    }

    suspend fun saveMessage(msg: MqttMessage) {
        messageDao.insert(msg.toEntity())
        enforceLimit()
    }

    suspend fun savePublishTopic(topic: String) {
        publishDao.insertOrUpdate(
            PublishHistoryEntity(topic = topic, lastUsedAt = System.currentTimeMillis())
        )
    }

    suspend fun deleteAllMessages() {
        messageDao.deleteAll()
    }

    private suspend fun enforceLimit(maxMessages: Int = 1000) {
        val count = messageDao.count()
        if (count > maxMessages) {
            messageDao.deleteOldest(count - maxMessages + 100)
        }
    }

    private fun MqttMessage.toEntity() = MessageHistoryEntity(
        topic = topic,
        payload = payload,
        qos = qos,
        retained = retained,
        isIncoming = isIncoming,
        isDelivered = isDelivered,
        timestamp = timestamp
    )

    private fun MessageHistoryEntity.toDomain() = MqttMessage(
        id = id,
        topic = topic,
        payload = payload,
        qos = qos,
        retained = retained,
        isIncoming = isIncoming,
        isDelivered = isDelivered,
        timestamp = timestamp
    )
}
