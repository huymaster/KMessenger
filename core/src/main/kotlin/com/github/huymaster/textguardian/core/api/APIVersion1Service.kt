package com.github.huymaster.textguardian.core.api

import com.github.huymaster.textguardian.core.api.type.AccessToken
import com.github.huymaster.textguardian.core.api.type.LoginRequest
import com.github.huymaster.textguardian.core.api.type.RefreshToken
import com.github.huymaster.textguardian.core.api.type.RegisterRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

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

    @POST("/api/v1/checkSession")
    suspend fun checkSession(@Body body: RefreshToken): Response<String>

    @DELETE("/api/v1/revoke")
    suspend fun revokeToken(@Body body: RefreshToken): Response<String>

    @POST("/api/v1/test")
    @Headers("Content-Disposition: attachment; filename=\\")
    suspend fun sendMessage(@Part body: MultipartBody.Part)
}