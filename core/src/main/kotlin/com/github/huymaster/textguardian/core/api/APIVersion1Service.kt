package com.github.huymaster.textguardian.core.api

import com.github.huymaster.textguardian.core.api.type.*
import retrofit2.Response
import retrofit2.http.*

interface APIVersion1Service {
    companion object {
        private const val PREFIX = "/api/v1"
    }

    @GET("$PREFIX/health")
    suspend fun health(): Response<Unit>

    @POST("$PREFIX/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<Unit>

    @POST("$PREFIX/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<RefreshToken>

    @GET("$PREFIX/auth/refresh")
    suspend fun refresh(@Query("refreshToken") refreshToken: String): Response<AccessToken>

    @DELETE("$PREFIX/auth/logout")
    suspend fun logout(
        @Header("Authorization") bearer: String,
        @Query("refreshToken") refreshToken: String
    ): Response<Unit>

    @GET("$PREFIX/user")
    suspend fun getUserInfo(@Header("Authorization") bearer: String): Response<UserInfo>

    @PUT("$PREFIX/user")
    suspend fun updateUserInfo(
        @Header("Authorization") bearer: String,
        @Body body: BasicUserInfo
    ): Response<List<String>>
}