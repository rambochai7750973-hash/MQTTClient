package com.example.mqttclient.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mqttclient.data.local.entity.PublishHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PublishHistoryDao {

    @Query("SELECT topic FROM publish_history ORDER BY lastUsedAt DESC LIMIT 10")
    fun getRecentTopics(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: PublishHistoryEntity)

    @Query("DELETE FROM publish_history WHERE topic = :topic")
    suspend fun deleteByTopic(topic: String)
}
