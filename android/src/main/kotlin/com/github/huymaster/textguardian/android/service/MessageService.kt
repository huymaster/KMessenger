package com.github.huymaster.textguardian.android.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.github.huymaster.textguardian.R
import com.github.huymaster.textguardian.android.MainActivity
import com.github.huymaster.textguardian.android.MainApplication
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessageService : FirebaseMessagingService() {
    private val notificationManager =
        MainApplication.instance.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val notification = message.notification
        if (notification == null) return
        val bubble = NotificationCompat.BubbleMetadata.Builder()
            .setIntent(
                PendingIntent.getActivity(
                    MainApplication.instance,
                    5,
                    Intent(MainApplication.instance, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setIcon(
                IconCompat.createWithResource(
                    MainApplication.instance,
                    R.drawable.ic_launcher_foreground
                )
            )
            .setDesiredHeight(480)
            .setAutoExpandBubble(true)
            .build()

        val shortcut = ShortcutInfoCompat.Builder(this, "shortcut_" + System.currentTimeMillis())
            .setLongLived(true)
            .setShortLabel("TextGuardian")
            .setIcon(
                IconCompat.createWithResource(
                    MainApplication.instance,
                    R.drawable.ic_launcher_foreground
                )
            )
            .setPerson(Person.Builder().setName("TextGuardian").build())
            .setIntent(
                Intent(MainApplication.instance, MainActivity::class.java)
            )
            .build()
        ShortcutManagerCompat.pushDynamicShortcut(this, shortcut)

        val notificationBuilder = NotificationCompat.Builder(this, "broadcast")
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setBubbleMetadata(bubble)
            .setShortcutId(shortcut.id)
            .setAutoCancel(true)
        notificationManager.notify(1, notificationBuilder.build())
    }
}