package com.github.huymaster

import com.github.huymaster.textguardian.core.dto.UserDTO
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.test.Test

class UnitTest {
    @Test
    fun test() {
        runBlocking { suspendTest() }
    }

    private suspend fun suspendTest() {
        val user = UserDTO()
        user.id = UUID.randomUUID()
        user.phoneNumber = ""
    }
}