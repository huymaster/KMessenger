package com.github.huymaster

import com.github.huymaster.textguardian.core.di.SharedModule
import com.github.huymaster.textguardian.core.dto.PublicKeyDTO
import com.github.huymaster.textguardian.server.data.table.PublicKeyTable
import com.github.huymaster.textguardian.server.di.Module
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import org.ktorm.database.Database
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList

class UnitTest : KoinTest {
    @JvmField
    @RegisterExtension
    val ext = KoinTestExtension.create {
        modules(SharedModule.api, SharedModule.security, SharedModule.objectMapper)
        modules(Module.database)
    }

    @Test
    fun test() {
        val db: Database by inject()
        val table = db.sequenceOf(PublicKeyTable)
        val list = table.toList()
            .associateWith { PublicKeyDTO() }
            .map { it.value.apply { importFrom(it.key) } }
        println(list)
    }
}