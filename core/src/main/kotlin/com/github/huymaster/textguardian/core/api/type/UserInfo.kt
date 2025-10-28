package com.github.huymaster.textguardian.core.api.type

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.huymaster.textguardian.core.entity.UserEntity
import java.time.Instant
import java.util.*

data class UserInfo(
    @field:JsonProperty("userId") val userId: UUID,
    @field:JsonProperty("phoneNumber") val phoneNumber: String,
    @field:JsonProperty("username") val username: String?,
    @field:JsonProperty("displayName") val displayName: String?,
    @field:JsonProperty("lastSeen") val lastSeen: Instant,
    @field:JsonProperty("createdAt") val createdAt: Instant,
    @field:JsonProperty("avatarUrl") val avatarUrl: String?,
    @field:JsonProperty("bio") val bio: String?
) {
    constructor(entity: UserEntity) : this(
        userId = entity.userId,
        phoneNumber = entity.phoneNumber,
        username = entity.username,
        displayName = entity.displayName,
        lastSeen = entity.lastSeen,
        createdAt = entity.createdAt,
        avatarUrl = entity.avatarUrl,
        bio = entity.bio
    )
}