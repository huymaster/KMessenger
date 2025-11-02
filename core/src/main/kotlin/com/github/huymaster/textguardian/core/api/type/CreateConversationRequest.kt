package com.github.huymaster.textguardian.core.api.type

import com.fasterxml.jackson.annotation.JsonProperty

data class CreateConversationRequest(
    @field:JsonProperty("name") val name: String
)
