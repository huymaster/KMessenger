package com.github.huymaster.textguardian.server.data.repository

import com.github.huymaster.textguardian.core.entity.UserEntity
import com.github.huymaster.textguardian.server.data.table.UserTable
import io.ktor.http.*
import org.ktorm.dsl.eq
import org.postgresql.util.PSQLException
import org.postgresql.util.PSQLState
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
        val normalizedUsername = username?.trimIndent()?.lowercase()
        if (normalizedUsername == null) {
            val updateResult = update({ it.userId eq userId }) { it.username = null }
            return if (updateResult != null)
                RepositoryResult.Success(Unit)
            else
                RepositoryResult.Success(Unit, HttpStatusCode.NoContent)
        }
        try {
            val updateResult = update({ it.userId eq userId }) { it.username = normalizedUsername }
            return if (updateResult != null)
                RepositoryResult.Success(Unit)
            else
                RepositoryResult.Success(Unit, HttpStatusCode.NoContent)
        } catch (e: PSQLException) {
            if (e.sqlState == PSQLState.UNIQUE_VIOLATION.state)
                return RepositoryResult.Error("Username already used", HttpStatusCode.Conflict)
            return RepositoryResult.Error("An internal error occurred", HttpStatusCode.InternalServerError)
        }
    }

    suspend fun updateDisplayName(userId: UUID, displayName: String?): RepositoryResult {
        val normalizedDisplayName = displayName?.trim()
        val updateResult: UserEntity? = update({ it.userId eq userId }) {
            it.displayName = normalizedDisplayName
        }
        return if (updateResult != null)
            RepositoryResult.Success(Unit, desiredStatus = HttpStatusCode.NoContent)
        else
            RepositoryResult.Success(Unit, HttpStatusCode.NoContent)
    }

    suspend fun updateLastSeen(userId: UUID, lastSeen: Instant?): RepositoryResult {
        if (lastSeen == null)
            return RepositoryResult.Success(Unit, HttpStatusCode.NoContent)
        val updateResult: UserEntity? = update({ it.userId eq userId }) {
            it.lastSeen = lastSeen
        }
        return if (updateResult != null)
            RepositoryResult.Success(Unit, desiredStatus = HttpStatusCode.NoContent)
        else
            RepositoryResult.Success(Unit, HttpStatusCode.NoContent)
    }

    suspend fun updateAvatar(userId: UUID, avatarUrl: String?): RepositoryResult {
        val updateResult: UserEntity? = update({ it.userId eq userId }) {
            it.avatarUrl = avatarUrl
        }
        return if (updateResult != null)
            RepositoryResult.Success(Unit, desiredStatus = HttpStatusCode.NoContent)
        else
            RepositoryResult.Success(Unit, HttpStatusCode.NoContent)
    }

    suspend fun updateBio(userId: UUID, bio: String?): RepositoryResult {
        val updateResult: UserEntity? = update({ it.userId eq userId }) {
            it.bio = bio
        }
        return if (updateResult != null)
            RepositoryResult.Success(Unit, desiredStatus = HttpStatusCode.NoContent)
        else
            RepositoryResult.Success(Unit, HttpStatusCode.NoContent)
    }
}