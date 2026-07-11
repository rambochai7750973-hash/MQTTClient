package com.mqtt.dashboard.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE connectionId = :connectionId ORDER BY timestamp DESC LIMIT 200")
    fun getMessages(connectionId: Long): Flow<List<MessageEntity>>

    @Insert
    suspend fun insert(message: MessageEntity): Long

    @Query("DELETE FROM messages WHERE connectionId = :connectionId")
    suspend fun deleteByConnectionId(connectionId: Long)

    @Delete
    suspend fun delete(message: MessageEntity)

    @Query("DELETE FROM messages WHERE id IN (SELECT id FROM messages WHERE connectionId = :connectionId ORDER BY timestamp ASC LIMIT :count)")
    suspend fun trimOldest(connectionId: Long, count: Int)
}
