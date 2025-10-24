package com.github.huymaster

import com.github.huymaster.textguardian.core.api.APIVersion1Service
import com.github.huymaster.textguardian.core.di.SharedModule
import com.github.huymaster.textguardian.server.di.Module
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.component.get
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension

class UnitTest : KoinTest {
    @JvmField
    @RegisterExtension
    val ext = KoinTestExtension.create {
        modules(SharedModule.api, SharedModule.security, SharedModule.objectMapper)
        modules(Module.database, Module.utils)
    }

    @Test
    fun test() {
        val service = get<APIVersion1Service>()
        runBlocking { println(service.health()) }
    }
}