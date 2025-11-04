package com.github.huymaster.textguardian.android.data.repository

import com.github.huymaster.textguardian.android.app.JWTTokenManager
import com.github.huymaster.textguardian.android.data.type.RepositoryResult
import com.github.huymaster.textguardian.core.api.APIVersion1Service
import com.github.huymaster.textguardian.core.api.type.ConversationInfo

class ConversationRepository(
    private val service: APIVersion1Service,
    private val tokenManager: JWTTokenManager
) {
    suspend fun getConversations(): RepositoryResult<List<ConversationInfo>> {
        return try {
            val token = runCatching {
                tokenManager.getAccessToken()!!
            }.onFailure {
                return RepositoryResult.Error()
            }.getOrThrow()
            val response = service.getConversations(token)
            if (response.isSuccessful) {
                RepositoryResult.Success(response.body()!!)
            } else {
                RepositoryResult.Error(message = response.errorBody()?.string())
            }
        } catch (e: Exception) {
            RepositoryResult.Error(message = e.message, throwable = e)
        }
    }
}