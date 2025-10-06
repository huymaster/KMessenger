package com.github.huymaster

import com.github.huymaster.textguardian.core.di.SharedModule
import com.github.huymaster.textguardian.server.di.Module
import com.github.huymaster.textguardian.server.utils.AttachmentCompressor
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.junit5.KoinTestExtension
import java.io.File

class UnitTest : KoinTest {
    @JvmField
    @RegisterExtension
    val ext = KoinTestExtension.create {
        modules(SharedModule.api, SharedModule.security, SharedModule.objectMapper)
        modules(Module.database, Module.utils)
    }

    @Test
    fun test() {
        val ac = get<AttachmentCompressor>()
        val file = File("/home/huymaster/logcat.txt")
    }
}