package com.github.huymaster.textguardian.server.data.repository

import com.github.huymaster.textguardian.core.entity.UserEntity
import com.github.huymaster.textguardian.server.data.table.UserTable
import org.ktorm.dsl.eq
import java.time.Instant
import java.util.*

class UserRepository() : BaseRepository<UserEntity, UserTable>(UserTable) {
    private suspend fun validatePhoneNumber(phoneNumber: String) {
        if (phoneNumber.length != 10)
            throw IllegalArgumentException("Phone number must be 10 digits")
        if (!phoneNumber.matches(Regex("^0[0-9]{9}$")))
            throw IllegalArgumentException("Phone number must start with 0")
        if (exists { it.phoneNumber eq phoneNumber })
            throw IllegalArgumentException("Phone number already used")
    }

    suspend fun newUser(phoneNumber: String): UserEntity? {
        validatePhoneNumber(phoneNumber)
        val entity = UserEntity()
        entity.userId = UUID.randomUUID()
        entity.phoneNumber = phoneNumber
        entity.lastSeen = Instant.now()
        entity.createdAt = Instant.now()
        entity.displayName = phoneNumber
        return create(entity)
    }

    suspend fun findUserById(id: UUID): UserEntity? {
        return find { it.userId eq id }
    }

    suspend fun findUserByPhoneNumber(phoneNumber: String): UserEntity? {
        return find { it.phoneNumber eq phoneNumber }
    }
}