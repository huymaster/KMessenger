package com.github.huymaster.textguardian.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import java.util.concurrent.atomic.AtomicReference

class MainApplication : Application() {
    companion object {
        private val _instanceHolder = AtomicReference<MainApplication>(null)
        val instance: MainApplication
            get() = _instanceHolder.get()!!
    }

    override fun onCreate() {
        _instanceHolder.set(this)
        super.onCreate()
        FirebaseApp.initializeApp(this)
        FirebaseMessaging.getInstance().subscribeToTopic("broadcast")
        val channel =
            NotificationChannel("broadcast", "Broadcast", NotificationManager.IMPORTANCE_HIGH)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}