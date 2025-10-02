package com.github.huymaster.textguardian.core.api.type

import com.fasterxml.jackson.annotation.JsonProperty

data class RegisterRequest(
    @field:JsonProperty("phoneNumber") val phoneNumber: String,
    @field:JsonProperty("password") val password: String
)