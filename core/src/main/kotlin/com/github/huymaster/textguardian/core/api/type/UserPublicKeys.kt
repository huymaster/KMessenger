package com.github.huymaster.textguardian.core.api.type

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.security.PublicKey
import java.util.*

data class UserPublicKeys(
    @get:JsonProperty("userId") val userId: UUID?,
    @get:JsonProperty("keys") val keys: Collection<String>
) {
    companion object {
        @JsonIgnore
        private val encoder = Base64.getEncoder()

        @JsonIgnore
        private val decoder = Base64.getDecoder()

        fun from(list: Collection<UserPublicKey>): UserPublicKeys {
            if (list.isEmpty())
                throw IllegalArgumentException("List is empty")
            if (!list.all { it.userId == list.first().userId })
                throw IllegalArgumentException("All public keys must have the same user id")
            return UserPublicKeys(list.first().userId, list.map { it.key })
        }
    }

    init {
        for (key in keys) {
            require(runCatching {
                UserPublicKey(userId, key)
            }.isSuccess) { "Invalid public key at #${keys.indexOf(key)}" }
        }
    }

    @JsonIgnore
    val keyList = keys.map { UserPublicKey(userId, it) }

    constructor(userId: UUID, vararg keys: String) : this(userId, keys.toList())
    constructor(userId: UUID, vararg keys: ByteArray) : this(userId, keys.map { encoder.encodeToString(it) })
    constructor(userId: UUID, vararg keys: PublicKey) : this(userId, keys.map { encoder.encodeToString(it.encoded) })
}