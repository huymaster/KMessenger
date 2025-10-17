package com.github.huymaster.textguardian.android.service

import android.app.Notification
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import com.github.huymaster.textguardian.android.MainApplication
import com.github.huymaster.textguardian.android.R
import com.github.huymaster.textguardian.android.app.AppSettingsManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MessageService : FirebaseMessagingService(), KoinComponent {
    private val settings: AppSettingsManager by inject()

    companion object {
        private val _messageFlow = MutableStateFlow<RemoteMessage?>(null)
        val messageFlow = _messageFlow.asStateFlow()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        settings.set(AppSettingsManager.Settings.FCM_TOKEN, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(1, buildNotification(message))
        if (MainApplication.ApplicationState.isForeground)
            _messageFlow.update { message }
    }

    private fun buildNotification(message: RemoteMessage): Notification {
        return NotificationCompat.Builder(this, "default")
            .setContentTitle(message.notification?.title ?: "KMessenger")
            .setContentText(message.notification?.body ?: message.data.toString())
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }
}