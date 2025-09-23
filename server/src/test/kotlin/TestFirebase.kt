package com.github.huymaster

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import java.io.File
import kotlin.test.Test
import kotlin.test.fail

class TestFirebase {
    @Test
    fun test() {
        val path = System.getenv("GOOGLE_APPLICATION_CREDENTIALS") ?: fail("GOOGLE_APPLICATION_CREDENTIALS not set")
        val stream = File(path).inputStream()
        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(stream))
            .setProjectId("kmessenger-c3976")
            .build()
        val app = FirebaseApp.initializeApp(options)
        val notification = FirebaseMessaging.getInstance(app)


        val n = Notification.builder()
            .setTitle("Hello")
            .setBody("Ban din nhan thong bao")
            .build()
        val message = Message.builder()
            .setNotification(n)
            .setTopic("broadcast")
            .build()
        notification.send(message)
    }
}