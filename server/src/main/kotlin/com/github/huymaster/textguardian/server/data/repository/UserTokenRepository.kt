package com.github.huymaster.textguardian.server.data.repository

import com.auth0.jwt.JWT
import com.github.huymaster.textguardian.core.api.type.AccessToken
import com.github.huymaster.textguardian.core.api.type.RefreshToken
import com.github.huymaster.textguardian.core.dto.UserDTO
import com.github.huymaster.textguardian.core.entity.UserTokenEntity
import com.github.huymaster.textguardian.server.data.table.UserTokenTable
import com.github.huymaster.textguardian.server.net.ALGORITHM
import io.ktor.http.*
import org.ktorm.dsl.eq
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class UserTokenRepository() : BaseRepository<UserTokenEntity, UserTokenTable>(UserTokenTable) {
    companion object {
        private const val TOKEN_LENGTH = 64
    }

    private val srandom = SecureRandom()
    private val encoder = Base64.getEncoder()

    private fun generateSecureToken(): String {
        val token = ByteArray(TOKEN_LENGTH)
        srandom.nextBytes(token)
        return encoder.encodeToString(token)
    }

    private fun createAccessToken(owner: UUID): String {
        return JWT.create()
            .withClaim(UserDTO.ID_FIELD, owner.toString())
            .withExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
            .sign(ALGORITHM)
    }

    suspend fun createToken(userId: UUID, deviceInfo: String? = null): RepositoryResult {
        val entity = UserTokenEntity()
        entity.userId = userId
        entity.refreshToken = generateSecureToken()
        entity.expiresAt = Instant.now().plus(30, ChronoUnit.DAYS)
        entity.deviceInfo = deviceInfo.let { if (it?.isBlank() == true) null else it }
        val result = create(entity)
        return if (result == null)
            RepositoryResult.Error(
                "Can't not create token. Please retry later",
                HttpStatusCode.InternalServerError
            )
        else
            RepositoryResult.Success(RefreshToken(entity.refreshToken))
    }

    suspend fun checkToken(refreshToken: String): RepositoryResult {
        val entity = find { it.refreshToken eq refreshToken }
        if (entity == null)
            return RepositoryResult.Error("Token not found", HttpStatusCode.NotFound)
        if (entity.expiresAt.isBefore(Instant.now()))
            return RepositoryResult.Error("Token expired", HttpStatusCode.Forbidden)
        if (entity.isRevoked)
            return RepositoryResult.Error("Token revoked", HttpStatusCode.Forbidden)
        return RepositoryResult.Success(entity)
    }

    suspend fun generateAccessToken(refreshToken: String): RepositoryResult {
        val checkResult = checkToken(refreshToken)
        if (checkResult !is RepositoryResult.Success<*>)
            return checkResult

        val owner = (checkResult.data as UserTokenEntity).userId
        val token = createAccessToken(owner)
        return RepositoryResult.Success(AccessToken(token))
    }

    suspend fun revokeToken(refreshToken: String, userId: UUID): RepositoryResult {
        val checkResult = checkToken(refreshToken)
        if (checkResult !is RepositoryResult.Success<*>)
            return checkResult
        if ((checkResult.data as UserTokenEntity).userId != userId)
            return RepositoryResult.Error("Token not belong to this user", HttpStatusCode.Unauthorized)
        if (update({ it.refreshToken eq refreshToken }) { it.isRevoked = true } == null)
            return RepositoryResult.Error(
                "Can't not revoke token. Please retry later",
                HttpStatusCode.InternalServerError
            )
        return RepositoryResult.Success(Unit)
    }
}