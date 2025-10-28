package com.github.huymaster.textguardian.core.api.type

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class UserPublicKey(
    @get:JsonProperty("userId") val userId: UUID,
    @get:JsonProperty("key") val key: ByteArray
) {
    constructor(userId: UUID, key: Array<Byte>) : this(userId, key.toByteArray())
}