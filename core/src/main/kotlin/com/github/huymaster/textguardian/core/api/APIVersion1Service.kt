package com.github.huymaster.textguardian.core.api

import com.github.huymaster.textguardian.core.api.type.*
import com.github.huymaster.textguardian.core.dto.ConversationDTO
import retrofit2.Response
import retrofit2.http.*

interface APIVersion1Service {
    companion object {
        private const val USER_AGENT = "KMessenger"
        private const val PREFIX = "/api/v1"
    }

    @POST("$PREFIX/register")
    suspend fun register(@Body body: RegisterRequest): Response<String>

    @POST("$PREFIX/login")
    suspend fun login(@Body body: LoginRequest): Response<RefreshToken>

    @POST("$PREFIX/refresh")
    suspend fun refreshToken(@Body body: RefreshToken): Response<AccessToken>

    @POST("$PREFIX/checkSession")
    suspend fun checkSession(@Body body: RefreshToken): Response<String>

    @DELETE("$PREFIX/revoke")
    suspend fun revokeToken(@Header("Authorization") token: String, @Body body: RefreshToken): Response<String>

    @GET("$PREFIX/conversations")
    suspend fun getConversationList(@Header("Authorization") token: String): Response<List<ConversationDTO>>

    @GET("$PREFIX/conversation")
    suspend fun getConversation(
        @Header("Authorization") token: String,
        @Query("conversationId") conversationId: String
    ): Response<ConversationDTO>

    @GET("$PREFIX/conversation/publicKeys")
    suspend fun getParticipantPublicKeys(
        @Header("Authorization") token: String,
        @Query("conversationId") conversationId: String
    ): Response<List<UserPublicKey>>
}