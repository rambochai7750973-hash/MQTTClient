package com.example.mqttclient.di

import android.content.Context
import androidx.room.Room
import com.example.mqttclient.data.local.AppDatabase
import com.example.mqttclient.data.local.dao.ConnectionConfigDao
import com.example.mqttclient.data.local.dao.MessageHistoryDao
import com.example.mqttclient.data.local.dao.PublishHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "mqtt_client.db"
        ).build()
    }

    @Provides
    fun provideConnectionConfigDao(db: AppDatabase): ConnectionConfigDao {
        return db.connectionConfigDao()
    }

    @Provides
    fun provideMessageHistoryDao(db: AppDatabase): MessageHistoryDao {
        return db.messageHistoryDao()
    }

    @Provides
    fun providePublishHistoryDao(db: AppDatabase): PublishHistoryDao {
        return db.publishHistoryDao()
    }
}
