package com.github.huymaster.textguardian.test

import com.github.huymaster.textguardian.core.di.SharedModule
import com.github.huymaster.textguardian.server.di.Module
import io.ktor.util.*
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension
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
        modules(SharedModule.api, SharedModule.security, SharedModule.objectMapper)
        modules(Module.utils, Module.database)
    }

    @Test
    suspend fun test() {
    }
}