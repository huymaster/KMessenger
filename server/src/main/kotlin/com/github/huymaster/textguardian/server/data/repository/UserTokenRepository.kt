package com.github.huymaster.textguardian.server.data.repository

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.github.huymaster.textguardian.core.dto.UserDTO
import com.github.huymaster.textguardian.core.entity.UserEntity
import com.github.huymaster.textguardian.core.entity.UserTokenEntity
import com.github.huymaster.textguardian.server.data.table.UserTokenTable
import com.github.huymaster.textguardian.server.net.AUDIENCE
import com.github.huymaster.textguardian.server.net.ISSUER
import com.github.huymaster.textguardian.server.net.JWT_SECRET
import org.ktorm.dsl.eq
import org.ktorm.schema.ColumnDeclaring
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class UserTokenRepository() : BaseRepository<UserTokenEntity, UserTokenTable>(UserTokenTable) {
    companion object {
        const val TOKEN_LENGTH = 64
        private val random = SecureRandom()
        private val encoder = Base64.getEncoder()
    }

    private fun generateRefreshToken(): String {
        val bytes = ByteArray(TOKEN_LENGTH)
        random.nextBytes(bytes)
        return encoder.encodeToString(bytes)
    }

    private fun generateAccessToken(userId: UUID): String {
        val token = JWT.create()
            .withAudience(AUDIENCE)
            .withIssuer(ISSUER)
            .withClaim(UserDTO.ID_FIELD, userId.toString())
            .withExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
            .sign(Algorithm.HMAC512(JWT_SECRET))
        return token
    }

    suspend fun createRefreshToken(user: UserEntity, deviceInfo: String? = null): UserTokenEntity? {
        val entity = UserTokenEntity().apply {
            this.id = user.id
            this.refreshToken = generateRefreshToken()
            this.deviceInfo = deviceInfo
            this.expiresAt = Instant.now().plus(30, ChronoUnit.DAYS)
            this.isRevoked = false
        }
        return create(entity)
    }

    suspend fun createAccessToken(refreshToken: String): String {
        if (!validateToken(refreshToken)) throw IllegalArgumentException("Invalid refresh token")
        val entity = find { it.refreshToken eq refreshToken } ?: throw IllegalArgumentException("Invalid refresh token")
        val userId = entity.id
        return generateAccessToken(userId)
    }

    suspend fun getTokens(user: UserEntity): List<UserTokenEntity> {
        val predicate: (UserTokenTable) -> ColumnDeclaring<Boolean> = { it.id eq user.id }
        return findAll(predicate)
    }

    suspend fun validateToken(refreshToken: String): Boolean {
        val predicate: (UserTokenTable) -> ColumnDeclaring<Boolean> = { it.refreshToken eq refreshToken }
        val entity = find(predicate) ?: return false
        if (entity.isRevoked)
            throw IllegalArgumentException("Token is revoked")
        if (entity.expiresAt < Instant.now())
            throw IllegalArgumentException("Token is expired")
        return true
    }

    suspend fun revokeToken(refreshToken: String) {
        val predicate: (UserTokenTable) -> ColumnDeclaring<Boolean> = { it.refreshToken eq refreshToken }
        updateAll(predicate) { it.isRevoked = true }
    }
}