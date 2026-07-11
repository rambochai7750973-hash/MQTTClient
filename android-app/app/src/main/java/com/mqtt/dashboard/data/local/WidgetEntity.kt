package com.mqtt.dashboard.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "widgets")
data class WidgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val connectionId: Long,
    val type: String,
    val name: String,
    val topic: String,
    val configJson: String = "{}",
    val position: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
