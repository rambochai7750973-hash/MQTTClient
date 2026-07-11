package com.mqtt.dashboard

import android.app.Application
import com.mqtt.dashboard.data.local.AppDatabase
import com.mqtt.dashboard.data.mqtt.MqttManager

class MqttDashboardApp : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    val mqttManager: MqttManager by lazy {
        MqttManager(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: MqttDashboardApp
            private set
    }
}
