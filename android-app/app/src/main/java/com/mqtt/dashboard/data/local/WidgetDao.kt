package com.mqtt.dashboard.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WidgetDao {
    @Query("SELECT * FROM widgets WHERE connectionId = :connectionId ORDER BY position ASC")
    fun getWidgets(connectionId: Long): Flow<List<WidgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(widget: WidgetEntity): Long

    @Update
    suspend fun update(widget: WidgetEntity)

    @Delete
    suspend fun delete(widget: WidgetEntity)

    @Query("DELETE FROM widgets WHERE id = :id")
    suspend fun deleteById(id: Long)
}
