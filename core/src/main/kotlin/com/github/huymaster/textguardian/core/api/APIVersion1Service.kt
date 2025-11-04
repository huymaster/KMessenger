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

    @GET("$PREFIX/auth/check")
    suspend fun check(@Header("Authorization") bearer: String): Response<Unit>

    @DELETE("$PREFIX/auth/logout")
    suspend fun logout(
        @Header("Authorization") bearer: String,
        @Query("refreshToken") refreshToken: String
    ): Response<Unit>

    @GET("$PREFIX/users")
    suspend fun findUsers(
        @Query("username") username: String? = null,
        @Query("phone") phone: String? = null
    ): Response<List<BasicUserInfo>>

    @GET("$PREFIX/user")
    suspend fun getUserInfo(@Header("Authorization") bearer: String): Response<UserInfo>

    @PUT("$PREFIX/user")
    suspend fun updateUserInfo(
        @Header("Authorization") bearer: String,
        @Body body: BasicUserInfo
    ): Response<List<String>>

    @GET("$PREFIX/conversations")
    suspend fun getConversations(@Header("Authorization") bearer: String): Response<List<ConversationInfo>>

    @POST("$PREFIX/conversation")
    suspend fun createConversation(
        @Header("Authorization") bearer: String,
        @Body body: CreateConversationRequest
    ): Response<ConversationInfo>

    @GET("$PREFIX/conversation/{id}")
    suspend fun getConversation(
        @Header("Authorization") bearer: String,
        @Path("id") id: String
    ): Response<ConversationInfo>

    @PUT("$PREFIX/conversation/{id}")
    suspend fun updateConversation(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
        @Body body: CreateConversationRequest
    ): Response<ConversationInfo>

    @DELETE("$PREFIX/conversation/{id}")
    suspend fun deleteConversation(
        @Header("Authorization") bearer: String,
        @Path("id") id: String
    ): Response<Unit>
}