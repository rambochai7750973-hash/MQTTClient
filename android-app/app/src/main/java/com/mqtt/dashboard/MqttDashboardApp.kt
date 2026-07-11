package com.mqtt.dashboard

import android.app.Application
import com.mqtt.dashboard.data.local.AppDatabase

class MqttDashboardApp : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
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
