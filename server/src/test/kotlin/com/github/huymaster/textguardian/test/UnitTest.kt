package com.github.huymaster.textguardian.test

import com.github.huymaster.textguardian.core.di.SharedModule
import com.github.huymaster.textguardian.server.data.repository.ConversationRepository
import com.github.huymaster.textguardian.server.di.Module
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import java.util.*

class UnitTest : KoinTest {
    @JvmField
    @RegisterExtension
    val ext = KoinTestExtension.create {
        modules(SharedModule.api, SharedModule.security, SharedModule.objectMapper)
        modules(Module.utils, Module.database)
    }

    @Test
    suspend fun test() {
        val var1: ConversationRepository by inject()
        val o = var1.newConversation(UUID.fromString("7f54d463-02d1-48b4-844d-548bb45755c5"), "Test")
        println(o)
    }
}