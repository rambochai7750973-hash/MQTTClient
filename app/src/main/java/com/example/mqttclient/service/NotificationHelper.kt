package com.example.mqttclient.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.mqttclient.MainActivity

class NotificationHelper(private val channelId: String) {

    fun buildConnectedNotification(
        context: android.content.Context,
        serverUri: String
    ): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, channelId)
            .setContentTitle("MQTT Client")
            .setContentText("Connected to $serverUri")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun buildDisconnectedNotification(context: android.content.Context): Notification {
        return NotificationCompat.Builder(context, channelId)
            .setContentTitle("MQTT Client")
            .setContentText("Disconnected")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun createNotificationChannel(context: android.content.Context) {
        val channel = android.app.NotificationChannel(
            channelId,
            "MQTT Connection",
            android.app.NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows MQTT connection status"
            setShowBadge(false)
        }
        val notificationManager =
            context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun showNotification(context: android.content.Context, id: Int, notification: Notification) {
        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (_: SecurityException) { }
    }
}
