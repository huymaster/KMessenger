package com.github.huymaster.textguardian.server.data.repository

import com.github.huymaster.textguardian.core.entity.CredentialEntity
import com.github.huymaster.textguardian.server.data.table.CredentialTable
import io.ktor.http.*
import org.ktorm.dsl.eq
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.security.spec.KeySpec
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class CredentialRepository() : BaseRepository<CredentialEntity, CredentialTable>(CredentialTable) {
    companion object {
        private const val ALGORITHM = "PBKDF2WithHmacSHA512"
        private const val ITERATIONS = 65535
        private const val KEY_LENGTH_IN_BITS = 1024
        private const val KEY_LENGTH = KEY_LENGTH_IN_BITS / 8
        private const val SALT_LENGTH = 32
    }

    private val srandom = SecureRandom()
    private val keyFactory = SecretKeyFactory.getInstance(ALGORITHM)
    private val passwordPattern = Regex("^[\\w_]{6,32}$")

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_LENGTH)
        srandom.nextBytes(salt)
        return salt
    }

    private fun generateHash(password: String, salt: ByteArray): ByteArray {
        val passCharArray = password.toCharArray()
        val specs: KeySpec = PBEKeySpec(passCharArray, salt, ITERATIONS, KEY_LENGTH_IN_BITS)
        return keyFactory.generateSecret(specs).encoded
    }

    fun hashPassword(password: String): ByteArray {
        require(password.isNotBlank()) { "Password can't empty" }
        val salt = generateSalt()
        val hash = generateHash(password, salt)
        val buffer = ByteBuffer.allocate(SALT_LENGTH + KEY_LENGTH)
        buffer.put(salt, 0, SALT_LENGTH)
        buffer.put(hash, 0, KEY_LENGTH)
        return buffer.array()
    }

    fun verifyPassword(password: String, hash: ByteArray): Boolean {
        if (password.isBlank() || hash.size != SALT_LENGTH + KEY_LENGTH) return false
        val salt = hash.copyOfRange(0, SALT_LENGTH)
        val hashToCheck = hash.copyOfRange(SALT_LENGTH, hash.size)
        val generatedHash = generateHash(password, salt)
        return generatedHash.contentEquals(hashToCheck)
    }

    suspend fun newCredential(userId: UUID, password: String): RepositoryResult {
        if (exists { it.userId eq userId }) return RepositoryResult.Error(
            "User already exists",
            HttpStatusCode.Conflict
        )
        if (!password.matches(passwordPattern)) return RepositoryResult.Error(
            "Password must be at least 6 characters long and contain only letters, numbers, and underscores",
            HttpStatusCode.BadRequest
        )

        val newCredential = CredentialEntity()
        newCredential.userId = userId
        newCredential.password = hashPassword(password)
        val result = create(newCredential) ?: return RepositoryResult.Error("Failed to create credential")
        return RepositoryResult.Success(result)
    }

    suspend fun verifyCredential(userId: UUID, password: String): RepositoryResult {
        val credential = find { it.userId eq userId }
        if (credential == null) return RepositoryResult.Error(
            "User not found",
            HttpStatusCode.NotFound
        )

        if (!verifyPassword(password, credential.password)) return RepositoryResult.Error(
            "Wrong password",
            HttpStatusCode.Unauthorized
        )

        return RepositoryResult.Success(Unit)
    }
}