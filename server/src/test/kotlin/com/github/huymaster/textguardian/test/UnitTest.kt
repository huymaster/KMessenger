package com.github.huymaster.textguardian.test

import com.github.huymaster.textguardian.core.api.APIVersion1Service
import com.github.huymaster.textguardian.core.di.SharedModule
import com.github.huymaster.textguardian.core.utils.createService
import com.github.huymaster.textguardian.server.data.repository.MessageRepository
import com.github.huymaster.textguardian.server.di.Module
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.util.*
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.api.fail
import org.koin.core.component.inject
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.junit5.KoinTestExtension
import java.security.KeyPair
import java.util.*

class CipherMessage(val id: UUID, val cipherText: String) {
    constructor(id: UUID, cipherText: ByteArray) : this(id, cipherText.encodeBase64())

    @get:BsonIgnore
    val bytes: ByteArray get() = cipherText.decodeBase64Bytes()
}

class UnitTest : KoinTest {
    @JvmField
    @RegisterExtension
    val ext = KoinTestExtension.create {
        modules(SharedModule.api, SharedModule.security, SharedModule.objectMapper, SharedModule.utils)
        modules(Module.utils, Module.database)
    }

    @Test
    suspend fun test_1() {
        val keyP = get<KeyPair>()
        val key = "fn1bOGGgiRwARUKVI0OzUUnw3r8MC8csU/VjwdomNMfv3qJLF75YKD1+XI7WsYK690DDFqlWXjvQRzoEirpYgw=="
        val api = createService(APIVersion1Service::class.java, "http://localhost:8080")
        val at = api.refresh(key).body() ?: fail { "Can't get access token" }
        val messages = api.getMessages(at.accessToken, "bb999bf0-5bef-4459-87d7-291603c38e86", null)
        println(messages)
    }

    @Test
    suspend fun test_2() {
        val db = get<MongoDatabase>()
        db.listCollectionNames().collect { println(it) }
    }

    @Test
    suspend fun test_3() {
        val mr: MessageRepository by inject()
    }
}