package com.example.mqttclient.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mqttclient.data.local.dao.ConnectionConfigDao
import com.example.mqttclient.data.local.dao.MessageHistoryDao
import com.example.mqttclient.data.local.dao.PublishHistoryDao
import com.example.mqttclient.data.local.entity.ConnectionConfigEntity
import com.example.mqttclient.data.local.entity.MessageHistoryEntity
import com.example.mqttclient.data.local.entity.PublishHistoryEntity

@Database(
    entities = [
        ConnectionConfigEntity::class,
        MessageHistoryEntity::class,
        PublishHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun connectionConfigDao(): ConnectionConfigDao
    abstract fun messageHistoryDao(): MessageHistoryDao
    abstract fun publishHistoryDao(): PublishHistoryDao
}
