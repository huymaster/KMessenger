package com.github.huymaster.textguardian.core.api.type

import com.fasterxml.jackson.annotation.JsonProperty

data class AccessToken(
    @field:JsonProperty("accessToken") val accessToken: String
)