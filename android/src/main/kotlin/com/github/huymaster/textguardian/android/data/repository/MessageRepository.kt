package com.github.huymaster.textguardian.android.data.repository

import com.github.huymaster.textguardian.android.app.JWTTokenManager
import com.github.huymaster.textguardian.android.data.type.RepositoryResult
import com.github.huymaster.textguardian.core.api.APIVersion1Service
import com.github.huymaster.textguardian.core.api.type.Message

class MessageRepository(
    private val service: APIVersion1Service,
    private val tokenManager: JWTTokenManager
) {
    suspend fun sendMessage(conversationId: String, message: Message): RepositoryResult<Unit> {
        val token = tokenManager.getAccessToken() ?: return RepositoryResult.Error(message = "Invalid token.")
        val response = service.sendMessage(token, conversationId, message)
        return if (response.isSuccessful)
            RepositoryResult.Success(Unit)
        else
            RepositoryResult.Error(
                message = "Failed to send message (${
                    response.errorBody()?.string() ?: "unknown error"
                })"
            )
    }

    suspend fun getMessages(conversationId: String, startMessageId: String?): RepositoryResult<List<Message>> {
        val token = tokenManager.getAccessToken() ?: return RepositoryResult.Error(message = "Invalid token.")
        val response = service.getMessages(token, conversationId, startMessageId)
        return if (response.isSuccessful)
            RepositoryResult.Success(response.body())
        else
            RepositoryResult.Error(
                message = "Failed to get messages (${
                    response.errorBody()?.string() ?: "unknown error"
                })"
            )
    }
}