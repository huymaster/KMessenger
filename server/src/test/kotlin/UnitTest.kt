package com.github.huymaster

import com.github.huymaster.textguardian.core.api.APIVersion1Service
import com.github.huymaster.textguardian.core.di.SharedModule
import com.github.huymaster.textguardian.core.security.KeyReconstruct
import com.github.huymaster.textguardian.server.di.Module
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.junit5.KoinTestExtension
import javax.swing.JOptionPane

class UnitTest : KoinTest {
    @JvmField
    @RegisterExtension
    val ext = KoinTestExtension.create {
        modules(SharedModule.api, SharedModule.security, SharedModule.objectMapper)
        modules(Module.database, Module.utils)
    }

    @Test
    fun test() {
        val api = get<APIVersion1Service>()
        runBlocking {
            val token = JOptionPane.showInputDialog("Enter token")
            val conversationId = "24dd9433-90e5-4bb0-8ede-bd4da654d773"
            val keys = api.getParticipantPublicKeys("Bearer $token", conversationId)
            println(keys)
            println(keys.body()?.map(KeyReconstruct::reconstructPublicKey))
        }
    }
}