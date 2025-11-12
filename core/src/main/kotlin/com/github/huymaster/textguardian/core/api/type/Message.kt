package com.github.huymaster.textguardian.core.api.type

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.util.*

data class Message(
    @get:JsonProperty("conversationId") val conversationId: UUID,
    @get:JsonProperty("content") val content: ByteArray,
    @get:JsonProperty("senderId") val senderId: UUID,
    @get:JsonProperty("sendAt") val sendAt: Instant,
    @get:JsonProperty("sessionKeys") val sessionKeys: Collection<String>,
    @get:JsonProperty("replyTo") val replyTo: UUID?
)