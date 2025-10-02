package com.github.huymaster.textguardian.server.data.repository

import com.github.huymaster.textguardian.core.entity.CredentialEntity
import com.github.huymaster.textguardian.core.entity.UserEntity
import com.github.huymaster.textguardian.server.data.table.CredentialTable
import org.ktorm.dsl.eq
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class CredentialRepository() : BaseRepository<CredentialEntity, CredentialTable>(CredentialTable) {
    companion object {
        private const val ALGORITHM = "PBKDF2WithHmacSHA512"
        private const val ITERATIONS = 65536
        private const val KEY_LENGTH = 1024
        private const val SALT_SIZE_BYTES = 64
        private val PASSWORD_REGEX = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,}$")
    }

    private val secureRandom = SecureRandom()
    private val secretKeyFactory = SecretKeyFactory.getInstance(ALGORITHM)

    private fun validatePassword(password: CharArray) {
        if (!PASSWORD_REGEX.matches(String(password)))
            throw IllegalArgumentException("Password must contain at least one lowercase letter, one uppercase letter, one digit, and be at least 8 characters long")
    }

    private fun generateSalt(): ByteArray = ByteArray(SALT_SIZE_BYTES).apply {
        secureRandom.nextBytes(this)
    }

    private fun hashPassword(password: CharArray, salt: ByteArray): ByteArray {
        val keySpec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH)
        password.fill(' ')
        return secretKeyFactory.generateSecret(keySpec).encoded
    }

    suspend fun checkCredential(user: UserEntity, passwordToCheck: String) =
        checkCredential(user, passwordToCheck.toCharArray())

    suspend fun checkCredential(user: UserEntity, passwordToCheck: CharArray): Boolean {
        val entity = find { it.id eq user.id } ?: return false
        val keySpec = PBEKeySpec(passwordToCheck, entity.key, ITERATIONS, KEY_LENGTH)
        passwordToCheck.fill(' ')
        val hash = secretKeyFactory.generateSecret(keySpec).encoded

        return MessageDigest.isEqual(hash, entity.password)
    }

    suspend fun createCredential(user: UserEntity, password: String) =
        createCredential(user, password.toCharArray())

    suspend fun createCredential(user: UserEntity, password: CharArray): CredentialEntity? {
        validatePassword(password)
        val newSalt = generateSalt()
        val hashedPassword = hashPassword(password.clone(), newSalt)

        val entity = CredentialEntity().apply {
            this.id = user.id
            this.key = newSalt
            this.password = hashedPassword
        }
        return create(entity)
    }
}