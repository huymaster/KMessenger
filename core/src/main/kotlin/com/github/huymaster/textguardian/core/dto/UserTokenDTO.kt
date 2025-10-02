package com.github.huymaster.textguardian.core.dto

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.huymaster.textguardian.core.entity.UserTokenEntity
import org.ktorm.entity.Entity
import java.time.Instant
import java.util.*

class UserTokenDTO(
    entity: UserTokenEntity? = null
) : BaseDTOImpl<UserTokenEntity>(entity) {
    companion object {
        const val ID_FIELD = "userId"
        const val REFRESH_TOKEN_FIELD = "refreshToken"
        const val DEVICE_INFO_FIELD = "deviceInfo"
        const val EXPIRES_AT_FIELD = "expiresAt"
        const val IS_REVOKED_FIELD = "isRevoked"
    }

    lateinit var id: UUID
    lateinit var refreshToken: String
    var deviceInfo: String? = null
    lateinit var expiresAt: Instant
    var isRevoked: Boolean = false

    override fun write(output: ObjectNode) {
        output.put(ID_FIELD, id.toString())
        output.put(REFRESH_TOKEN_FIELD, refreshToken)
        output.put(DEVICE_INFO_FIELD, deviceInfo)
        output.put(EXPIRES_AT_FIELD, expiresAt.toEpochMilli())
        output.put(IS_REVOKED_FIELD, isRevoked)
    }

    override fun read(input: JsonNode) {
        id = UUID.fromString(input.getOrThrow(ID_FIELD).asText())
        refreshToken = input.getOrThrow(REFRESH_TOKEN_FIELD).asText()
        deviceInfo = input.getOrNull(DEVICE_INFO_FIELD)?.asText()
        expiresAt = input.getOrDefault(EXPIRES_AT_FIELD, 0L).asLong().let { Instant.ofEpochMilli(it) }
        isRevoked = input.getOrDefault(IS_REVOKED_FIELD, false).asBoolean()
    }

    override fun toEntity(): UserTokenEntity = Entity.create<UserTokenEntity>().apply {
        this.id = this@UserTokenDTO.id
        this.refreshToken = this@UserTokenDTO.refreshToken
        this.deviceInfo = this@UserTokenDTO.deviceInfo
        this.expiresAt = this@UserTokenDTO.expiresAt
        this.isRevoked = this@UserTokenDTO.isRevoked
    }

    override fun toDTO(entity: UserTokenEntity): BaseDTO<UserTokenEntity> = UserTokenDTO().apply {
        this.id = entity.id
        this.refreshToken = entity.refreshToken
        this.deviceInfo = entity.deviceInfo
        this.expiresAt = entity.expiresAt
        this.isRevoked = entity.isRevoked
    }

    override fun mergeTo(entity: UserTokenEntity) {
        entity.id = id
        entity.refreshToken = refreshToken
        entity.deviceInfo = deviceInfo
        entity.expiresAt = expiresAt
        entity.isRevoked = isRevoked
    }

    override fun importFrom(entity: UserTokenEntity) {
        id = entity.id
        refreshToken = entity.refreshToken
        deviceInfo = entity.deviceInfo
        expiresAt = entity.expiresAt
        isRevoked = entity.isRevoked
    }
}