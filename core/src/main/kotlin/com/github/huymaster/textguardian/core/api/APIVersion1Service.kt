package com.github.huymaster.textguardian.core.api

import com.github.huymaster.textguardian.core.api.type.AccessToken
import com.github.huymaster.textguardian.core.api.type.LoginRequest
import com.github.huymaster.textguardian.core.api.type.RefreshToken
import com.github.huymaster.textguardian.core.api.type.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface APIVersion1Service {
    companion object {
        private const val USER_AGENT = "KMessenger"
    }

    @POST("/api/v1/register")
    suspend fun register(@Body body: RegisterRequest): Response<String>

    @POST("/api/v1/login")
    suspend fun login(@Body body: LoginRequest): Response<RefreshToken>

    @POST("/api/v1/refresh")
    suspend fun refreshToken(@Body body: RefreshToken): Response<AccessToken>
}