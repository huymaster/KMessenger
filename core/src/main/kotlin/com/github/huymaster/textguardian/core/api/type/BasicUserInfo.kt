package com.github.huymaster.textguardian.core.api.type

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.huymaster.textguardian.core.entity.UserEntity
import java.time.Instant

data class BasicUserInfo(
    @field:JsonProperty("phoneNumber") val phoneNumber: String? = null,
    @field:JsonProperty("username") val username: String? = null,
    @field:JsonProperty("displayName") val displayName: String? = null,
    @field:JsonProperty("lastSeen") val lastSeen: Instant? = null,
    @field:JsonProperty("createdAt") val createdAt: Instant? = null,
    @field:JsonProperty("avatarUrl") val avatarUrl: String? = null,
    @field:JsonProperty("bio") val bio: String? = null
) {
    constructor(entity: UserEntity, noPhoneNumber: Boolean = false) : this(
        phoneNumber = if (noPhoneNumber) null else entity.phoneNumber,
        username = entity.username,
        displayName = entity.displayName,
        lastSeen = entity.lastSeen,
        createdAt = entity.createdAt,
        avatarUrl = entity.avatarUrl,
        bio = entity.bio
    )
}