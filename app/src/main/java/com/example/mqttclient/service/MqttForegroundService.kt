package com.example.mqttclient.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class MqttForegroundService : Service() {

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "mqtt_connection"
        const val ACTION_DISCONNECT = "com.example.mqttclient.ACTION_DISCONNECT"
    }

    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(CHANNEL_ID)
        notificationHelper.createNotificationChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = notificationHelper.buildConnectedNotification(this, "MQTT")
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
    }
}
