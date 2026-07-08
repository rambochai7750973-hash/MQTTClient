package com.example.mqttclient.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.mqttclient.data.local.entity.MessageHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageHistoryDao {

    @Query("SELECT * FROM message_history ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    fun getMessagesPaged(limit: Int, offset: Int): Flow<List<MessageHistoryEntity>>

    @Query("SELECT * FROM message_history WHERE topic LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchMessages(query: String): Flow<List<MessageHistoryEntity>>

    @Query("SELECT * FROM message_history WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getMessagesByTimeRange(start: Long, end: Long): Flow<List<MessageHistoryEntity>>

    @Insert
    suspend fun insert(message: MessageHistoryEntity): Long

    @Query("DELETE FROM message_history WHERE id IN (SELECT id FROM message_history ORDER BY timestamp ASC LIMIT :count)")
    suspend fun deleteOldest(count: Int)

    @Query("DELETE FROM message_history")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM message_history")
    suspend fun count(): Int
}
