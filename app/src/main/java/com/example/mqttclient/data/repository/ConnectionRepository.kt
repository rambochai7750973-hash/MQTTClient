package com.example.mqttclient.data.repository

import com.example.mqttclient.data.local.dao.ConnectionConfigDao
import com.example.mqttclient.data.model.ConnectionConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionRepository @Inject constructor(
    private val configDao: ConnectionConfigDao
) {
    fun getAll(): Flow<List<ConnectionConfig>> {
        return configDao.getAll().map { entities ->
            entities.map { ConnectionConfig.fromEntity(it) }
        }
    }

    suspend fun getById(id: Long): ConnectionConfig? {
        return configDao.getById(id)?.let { ConnectionConfig.fromEntity(it) }
    }

    suspend fun save(config: ConnectionConfig): Long {
        return configDao.insert(config.toEntity())
    }

    suspend fun update(config: ConnectionConfig) {
        configDao.update(config.toEntity())
    }

    suspend fun delete(config: ConnectionConfig) {
        configDao.delete(config.toEntity())
    }

    suspend fun deleteById(id: Long) {
        configDao.deleteById(id)
    }
}
