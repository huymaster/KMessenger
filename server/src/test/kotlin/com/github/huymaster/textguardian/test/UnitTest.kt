package com.github.huymaster.textguardian.test

import com.github.huymaster.textguardian.core.api.APIVersion1Service
import com.github.huymaster.textguardian.core.di.SharedModule
import com.github.huymaster.textguardian.core.utils.createService
import com.github.huymaster.textguardian.server.di.Module
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension

class UnitTest : KoinTest {
    @JvmField
    @RegisterExtension
    val ext = KoinTestExtension.create {
        modules(SharedModule.api, SharedModule.security, SharedModule.objectMapper)
        modules(Module.utils, Module.database)
    }

    @Test
    fun test() {
        val svc = createService(APIVersion1Service::class, "http://localhost:8080")
        runBlocking {
            val token =
                svc.refresh("KGuUkGNL/4Ak9R+u3LJTk14bX6QG8reIR1FVIyHDyplisaecjdh1Sl/5ZTezN/XIzjU4qjEyD/L9eIn4el5s7A==")
            val tk = token.body()
            assertNotNull(tk)
            val info = svc.getUserInfo(tk.accessToken)
            println(info)
            println(info.body())
            println(info.errorBody()?.string())
        }
    }
}