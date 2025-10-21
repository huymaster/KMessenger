package com.github.huymaster.textguardian.core.api

import com.github.huymaster.textguardian.core.api.type.AccessToken
import com.github.huymaster.textguardian.core.api.type.LoginRequest
import com.github.huymaster.textguardian.core.api.type.RefreshToken
import com.github.huymaster.textguardian.core.api.type.RegisterRequest
import retrofit2.http.*

interface APIVersion1Service {
    companion object {
        private const val USER_AGENT = "KMessenger"
        private const val PREFIX = "/api/v1"
    }

    @POST("$PREFIX/auth/register")
    suspend fun register(@Body body: RegisterRequest): String

    @POST("$PREFIX/auth/login")
    suspend fun login(@Body body: LoginRequest): RefreshToken

    @GET("$PREFIX/auth/refresh")
    suspend fun refresh(@Header("Authorization") token: String, @Body body: RefreshToken): AccessToken

    @DELETE("$PREFIX/auth/logout")
    suspend fun logout(@Header("Authorization") token: String, @Body body: RefreshToken): String
}