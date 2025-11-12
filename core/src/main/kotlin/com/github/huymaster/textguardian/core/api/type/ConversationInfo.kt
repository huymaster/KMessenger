package com.github.huymaster.textguardian.core.api.type

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.huymaster.textguardian.core.entity.ConversationEntity
import java.time.Instant

data class ConversationInfo(
    @field:JsonProperty("conversationId") val conversationId: String,
    @field:JsonProperty("name") val name: String,
    @field:JsonProperty("creator") val creator: String,
    @field:JsonProperty("createdAt") val createdAt: Instant,
    @field:JsonProperty("lastUpdated") val lastUpdated: Instant
) {
    constructor(conversationEntity: ConversationEntity) : this(
        conversationEntity.conversationId.toString(),
        conversationEntity.name,
        conversationEntity.creator.toString(),
        conversationEntity.createdAt,
        conversationEntity.lastUpdated
    )
}
