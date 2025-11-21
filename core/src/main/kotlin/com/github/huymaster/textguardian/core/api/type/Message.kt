package com.github.huymaster.textguardian.core.api.type

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.util.*

data class Message(
    @get:JsonProperty("id") val id: UUID,
    @get:JsonProperty("content") val content: ByteArray,
    @get:JsonProperty("senderId") val senderId: UUID? = null,
    @get:JsonProperty("keys") val sessionKeys: Array<ByteArray>,
    @get:JsonProperty("sendAt") val sendAt: Instant? = null,
    @get:JsonProperty("replyTo") val replyTo: UUID? = null,
    @get:JsonProperty("attachments") val attachments: Collection<UUID> = emptyList()
)