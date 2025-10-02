package com.github.huymaster.textguardian.core.api.type

import com.fasterxml.jackson.annotation.JsonProperty

data class RefreshToken(
    @field:JsonProperty("refreshToken") val refreshToken: String
)