package com.example.mqttclient.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mqttclient.data.local.entity.ConnectionConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectionConfigDao {

    @Query("SELECT * FROM connection_config ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<ConnectionConfigEntity>>

    @Query("SELECT * FROM connection_config WHERE id = :id")
    suspend fun getById(id: Long): ConnectionConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: ConnectionConfigEntity): Long

    @Update
    suspend fun update(config: ConnectionConfigEntity)

    @Delete
    suspend fun delete(config: ConnectionConfigEntity)

    @Query("DELETE FROM connection_config WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM connection_config")
    suspend fun count(): Int
}
