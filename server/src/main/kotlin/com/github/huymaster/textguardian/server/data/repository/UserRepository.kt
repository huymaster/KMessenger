package com.github.huymaster.textguardian.server.data.repository

import com.github.huymaster.textguardian.core.entity.UserEntity
import com.github.huymaster.textguardian.server.data.table.UserTable
import io.ktor.http.*
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.neq
import java.time.Instant
import java.util.*

class UserRepository() : BaseRepository<UserEntity, UserTable>(UserTable) {
    private val phonePattern = Regex("^0\\d{9}$")

    private fun validatePhoneNumber(phone: String): Boolean =
        phone.matches(phonePattern)

    suspend fun newUser(phone: String): RepositoryResult {
        if (exists { it.phoneNumber eq phone })
            return RepositoryResult.Error("Phone number already used", HttpStatusCode.Conflict)

        if (!validatePhoneNumber(phone))
            return RepositoryResult.Error("Phone number must start with 0 and have 10 digits")

        val entity = create(UserEntity().apply {
            this.userId = UUID.randomUUID()
            this.phoneNumber = phone
            this.createdAt = Instant.now()
            this.lastSeen = Instant.EPOCH
        })
        if (entity == null)
            return RepositoryResult.Error("Failed to create user", HttpStatusCode.InternalServerError)
        return RepositoryResult.Success(entity)
    }

    suspend fun getUserByUserId(userId: String?): RepositoryResult {
        val uid = runCatching { UUID.fromString(userId) }.getOrNull()
        if (uid == null)
            return RepositoryResult.Error("Invalid user id", HttpStatusCode.BadRequest)
        val entity = find { it.userId eq uid }
        if (entity == null)
            return RepositoryResult.Error("User not found", HttpStatusCode.NotFound)
        return RepositoryResult.Success(entity)
    }

    suspend fun getUserByUserId(userId: UUID): RepositoryResult {
        val entity = find { it.userId eq userId }
        if (entity == null)
            return RepositoryResult.Error("User not found", HttpStatusCode.NotFound)
        return RepositoryResult.Success(entity)
    }

    suspend fun getUserByPhoneNumber(phoneNumber: String): RepositoryResult {
        val entity = find { it.phoneNumber eq phoneNumber }
        if (entity == null)
            return RepositoryResult.Error("User not found", HttpStatusCode.NotFound)
        return RepositoryResult.Success(entity)
    }

    suspend fun getUserByUsername(username: String): RepositoryResult {
        val entity = find { it.username eq username }
        if (entity == null)
            return RepositoryResult.Error("User not found", HttpStatusCode.NotFound)
        return RepositoryResult.Success(entity)
    }

    suspend fun updateUsername(userId: UUID, username: String?): RepositoryResult {
        val findResult = getUserByUserId(userId)
        if (findResult !is RepositoryResult.Success<*>)
            return findResult
        val updateResult =
            update({ (it.userId eq userId) and (it.username neq (username ?: "")) }) { it.username = username }
        if (updateResult == null)
            return RepositoryResult.Success(
                Unit,
                desiredStatus = HttpStatusCode.NoContent
            )
        return RepositoryResult.Success(Unit)
    }
}