package com.github.huymaster.textguardian.core.api

import com.github.huymaster.textguardian.core.api.type.*
import retrofit2.Response
import retrofit2.http.*
import java.util.*

interface APIVersion1Service {
    companion object {
        private const val PREFIX = "/api/v1"
    }

    @GET("$PREFIX/health")
    suspend fun health(): Response<Unit>

    @POST("$PREFIX/auth/register")
    suspend fun register(
        @Body body: RegisterRequest
    ): Response<Unit>

    @POST("$PREFIX/auth/login")
    suspend fun login(
        @Body body: LoginRequest
    ): Response<RefreshToken>

    @GET("$PREFIX/auth/refresh")
    suspend fun refresh(
        @Query("refreshToken") refreshToken: String
    ): Response<AccessToken>

    @GET("$PREFIX/auth/check")
    suspend fun check(
        @Header("Authorization") bearer: String
    ): Response<Unit>

    @DELETE("$PREFIX/auth/logout")
    suspend fun logout(
        @Header("Authorization") bearer: String
    ): Response<Unit>

    @GET("$PREFIX/users")
    suspend fun findUsers(
        @Query("username") username: String? = null,
        @Query("phoneNumber") phone: String? = null
    ): Response<List<UserInfo>>

    @GET("$PREFIX/user")
    suspend fun getUserInfo(
        @Header("Authorization") bearer: String,
        @Query("userId") userId: String? = null
    ): Response<UserInfo>

    @PUT("$PREFIX/user")
    suspend fun updateUserInfo(
        @Header("Authorization") bearer: String,
        @Body body: UserInfo
    ): Response<List<String>>

    @GET("$PREFIX/keys")
    suspend fun getPublicKeys(
        @Query("userId") userId: String
    ): Response<UserPublicKeys>

    @POST("$PREFIX/key")
    suspend fun addPublicKey(
        @Header("Authorization") bearer: String,
        @Body body: UserPublicKey
    ): Response<Unit>

    @GET("$PREFIX/conversations")
    suspend fun getConversations(
        @Header("Authorization") bearer: String
    ): Response<List<ConversationInfo>>

    @POST("$PREFIX/conversation")
    suspend fun createConversation(
        @Header("Authorization") bearer: String,
        @Body body: CreateConversationRequest
    ): Response<UUID>

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

    @GET("$PREFIX/conversation/{id}/participants")
    suspend fun getParticipants(
        @Header("Authorization") bearer: String,
        @Path("id") id: String
    ): Response<List<ParticipantInfo>>

    @POST("$PREFIX/participant")
    suspend fun addParticipant(
        @Header("Authorization") bearer: String,
        @Body body: ParticipantInfo
    ): Response<Unit>

    @DELETE("$PREFIX/participant")
    suspend fun removeParticipant(
        @Header("Authorization") bearer: String,
        @Body body: ParticipantInfo
    ): Response<Unit>

    @POST("$PREFIX/message/{id}")
    suspend fun sendMessage(
        @Header("Authorization") bearer: String,
        @Path("id") conversationId: String,
        @Body body: Message
    ): Response<Unit>

    @GET("$PREFIX/message/{cid}/{mid}")
    suspend fun getMessage(
        @Header("Authorization") bearer: String,
        @Path("cid") conversationId: String,
        @Path("mid") messageId: String
    )

    @GET("$PREFIX/message/{id}")
    suspend fun getMessages(
        @Header("Authorization") bearer: String,
        @Path("id") conversationId: String,
        @Query("from") startMessageId: String?
    ): Response<List<Message>>

    @GET("$PREFIX/message/{id}/latest")
    suspend fun getLastestMessages(
        @Header("Authorization") bearer: String,
        @Path("id") conversationId: String,
        @Query("since") since: String?
    ): Response<List<Message>>
}