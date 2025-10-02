package com.github.huymaster.textguardian.core.dto

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.huymaster.textguardian.core.entity.UserEntity
import org.ktorm.entity.Entity
import java.time.Instant
import java.util.*

class UserDTO(
    entity: UserEntity? = null
) : BaseDTOImpl<UserEntity>(entity) {
    companion object {
        const val ID_FIELD = "userId"
        const val PHONE_NUMBER_FIELD = "phoneNumber"
        const val USERNAME_FIELD = "username"
        const val DISPLAY_NAME_FIELD = "displayName"
        const val LAST_SEEN_FIELD = "lastSeen"
        const val CREATED_AT_FIELD = "createdAt"
        const val AVATAR_URL_FIELD = "avatarUrl"
        const val BIO_FIELD = "bio"
    }

    lateinit var id: UUID
    lateinit var phoneNumber: String
    var username: String? = null
    var displayName: String? = null
    var lastSeen: Instant = Instant.EPOCH
    var createdAt: Instant = Instant.EPOCH
    var avatarUrl: String? = null
    var bio: String? = null

    override fun write(output: ObjectNode) {
        output.put(ID_FIELD, id.toString())
        output.put(PHONE_NUMBER_FIELD, phoneNumber)
        output.put(USERNAME_FIELD, username)
        output.put(DISPLAY_NAME_FIELD, displayName)
        output.put(LAST_SEEN_FIELD, lastSeen.toEpochMilli())
        output.put(CREATED_AT_FIELD, createdAt.toEpochMilli())
        output.put(AVATAR_URL_FIELD, avatarUrl)
        output.put(BIO_FIELD, bio)
    }

    override fun read(input: JsonNode) {
        id = UUID.fromString(input.getOrThrow(ID_FIELD).asText())
        phoneNumber = input.getOrThrow(PHONE_NUMBER_FIELD).asText()
        username = input.getOrNull(USERNAME_FIELD)?.asText()
        displayName = input.getOrNull(DISPLAY_NAME_FIELD)?.asText()
        lastSeen = input.getOrDefault(LAST_SEEN_FIELD, 0L).asLong().let { Instant.ofEpochMilli(it) }
        createdAt = input.getOrDefault(CREATED_AT_FIELD, 0L).asLong().let { Instant.ofEpochMilli(it) }
        avatarUrl = input.getOrNull(AVATAR_URL_FIELD)?.asText()
        bio = input.getOrNull(BIO_FIELD)?.asText()
    }

    override fun toEntity(): UserEntity =
        Entity.create<UserEntity>().apply {
            this.id = this@UserDTO.id
            this.phoneNumber = this@UserDTO.phoneNumber
            this.username = this@UserDTO.username
            this.displayName = this@UserDTO.displayName
            this.lastSeen = this@UserDTO.lastSeen
            this.createdAt = this@UserDTO.createdAt
            this.avatarUrl = this@UserDTO.avatarUrl
            this.bio = this@UserDTO.bio
        }

    override fun toDTO(entity: UserEntity): BaseDTO<UserEntity> =
        UserDTO().apply {
            this.id = entity.id
            this.phoneNumber = entity.phoneNumber
            this.username = entity.username
            this.displayName = entity.displayName
            this.lastSeen = entity.lastSeen
            this.createdAt = entity.createdAt
            this.avatarUrl = entity.avatarUrl
            this.bio = entity.bio
        }

    override fun mergeTo(entity: UserEntity) {
        entity.id = id
        entity.phoneNumber = phoneNumber
        entity.username = username
        entity.displayName = displayName
        entity.lastSeen = lastSeen
        entity.createdAt = createdAt
        entity.avatarUrl = avatarUrl
        entity.bio = bio
    }

    override fun importFrom(entity: UserEntity) {
        id = entity.id
        phoneNumber = entity.phoneNumber
        username = entity.username
        displayName = entity.displayName
        lastSeen = entity.lastSeen
        createdAt = entity.createdAt
        avatarUrl = entity.avatarUrl
        bio = entity.bio
    }
}