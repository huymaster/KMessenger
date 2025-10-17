package com.github.huymaster

import com.github.huymaster.textguardian.core.api.APIVersion1Service
import com.github.huymaster.textguardian.core.api.type.RefreshToken
import com.github.huymaster.textguardian.core.di.SharedModule
import com.github.huymaster.textguardian.core.entity.ConversationEntity
import com.github.huymaster.textguardian.server.di.Module
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.get
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
        val svc = get<APIVersion1Service>()
        runBlocking {
            val token =
                svc.refreshToken(RefreshToken("e6pFRejm7RzsAfm+YMfpYzY8tz4lHb75DukGTM0ABOsIjpJKyhkhKbQs6395nRXOffS0HKAkGiHwWCHLiW8PSQ=="))
            return@runBlocking svc.getConversation(
                "Bearer ${token.body()!!.accessToken}",
                "de728948-e96a-4c9b-b6d6-c8388d562670"
            )
        }.let { response ->
            println(response.code())
            println(response.body()?.let { ConversationEntity().apply { it.exportTo(this) } })
            println(response.errorBody()?.string())
        }
    }
}