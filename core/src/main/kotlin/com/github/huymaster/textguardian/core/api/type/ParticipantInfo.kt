package com.github.huymaster.textguardian.core.api.type

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.huymaster.textguardian.core.entity.ParticipantEntity

data class ParticipantInfo(
    @field:JsonProperty("conversationId") val conversationId: String,
    @field:JsonProperty("userId") val userId: String
) {
    constructor(entity: ParticipantEntity) : this(entity.conversationId.toString(), entity.userId.toString())
}
