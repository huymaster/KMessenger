package com.github.huymaster.textguardian.core.api.type

import com.fasterxml.jackson.annotation.JsonProperty

data class LoginRequest(
    @field:JsonProperty("phoneNumber") val phoneNumber: String,
    @field:JsonProperty("password") val password: String
)