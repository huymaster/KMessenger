package com.github.huymaster.textguardian.core.api.type

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.huymaster.textguardian.core.entity.ConversationEntity
import java.time.Instant
import java.util.*

data class ConversationInfo(
    @field:JsonProperty("conversationId") val conversationId: UUID,
    @field:JsonProperty("name") val name: String,
    @field:JsonProperty("creator") val creator: UUID,
    @field:JsonProperty("createdAt") val createdAt: Instant,
    @field:JsonProperty("lastUpdated") val lastUpdated: Instant
) {
    constructor(conversationEntity: ConversationEntity) : this(
        conversationEntity.conversationId,
        conversationEntity.name,
        conversationEntity.creator,
        conversationEntity.createdAt,
        conversationEntity.lastUpdated
    )
}
