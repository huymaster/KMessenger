package com.github.huymaster.textguardian.android.data.repository

import com.github.huymaster.textguardian.android.app.CipherManager
import com.github.huymaster.textguardian.android.app.JWTTokenManager
import com.github.huymaster.textguardian.android.data.type.RepositoryResult
import com.github.huymaster.textguardian.core.api.APIVersion1Service
import com.github.huymaster.textguardian.core.api.type.ConversationInfo
import com.github.huymaster.textguardian.core.api.type.CreateConversationRequest
import com.github.huymaster.textguardian.core.api.type.ParticipantInfo
import org.koin.core.component.KoinComponent
import java.util.*

class ConversationRepository(
    private val service: APIVersion1Service,
    private val cipherManager: CipherManager,
    private val tokenManager: JWTTokenManager
) : KoinComponent {
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

    suspend fun getConversation(conversationId: String): RepositoryResult<ConversationInfo> {
        return try {
            val token = runCatching {
                tokenManager.getAccessToken()!!
            }.onFailure {
                return RepositoryResult.Error()
            }.getOrThrow()
            val response = service.getConversation(token, conversationId)
            if (response.isSuccessful) {
                RepositoryResult.Success(response.body())
            } else {
                RepositoryResult.Error(message = response.errorBody()?.string())
            }
        } catch (e: Exception) {
            RepositoryResult.Error(message = e.message, throwable = e)
        }
    }

    suspend fun getLatestMessage(conversationId: String): RepositoryResult<String?> {
        return try {
            val token = runCatching {
                tokenManager.getAccessToken()!!
            }.onFailure {
                return RepositoryResult.Error()
            }.getOrThrow()
            val response = service.getMessages(token, conversationId, null)
            if (response.isSuccessful) {
                val msg = response.body()?.firstOrNull() ?: return RepositoryResult.Error()
                val decripted = cipherManager.decrypt(msg)
                RepositoryResult.Success(decripted)
            } else {
                RepositoryResult.Error(message = response.errorBody()?.string())
            }
        } catch (e: Exception) {
            RepositoryResult.Error(message = e.message, throwable = e)
        }
    }

    suspend fun createConversation(name: String): RepositoryResult<UUID> {
        return try {
            val token = runCatching {
                tokenManager.getAccessToken()!!
            }.onFailure {
                return RepositoryResult.Error(throwable = it)
            }.getOrThrow()
            val response = service.createConversation(token, CreateConversationRequest(name))
            if (response.isSuccessful) {
                RepositoryResult.Success(response.body())
            } else {
                RepositoryResult.Error(message = response.errorBody()?.string())
            }
        } catch (e: Exception) {
            RepositoryResult.Error(message = e.message, throwable = e)
        }
    }

    suspend fun getParticipants(conversationId: String): RepositoryResult<List<ParticipantInfo>> {
        return try {
            val token = runCatching {
                tokenManager.getAccessToken()!!
            }.onFailure {
                return RepositoryResult.Error()
            }.getOrThrow()
            val response = service.getParticipants(token, conversationId)
            if (response.isSuccessful) {
                RepositoryResult.Success(response.body()!!)
            } else {
                RepositoryResult.Error(message = response.errorBody()?.string())
            }
        } catch (e: Exception) {
            RepositoryResult.Error(message = e.message, throwable = e)
        }
    }

    suspend fun addParticipant(conversationId: String, participantId: String): RepositoryResult<Nothing> {
        return try {
            val token = runCatching {
                tokenManager.getAccessToken()!!
            }.onFailure {
                return RepositoryResult.Error()
            }.getOrThrow()
            val response = service.addParticipant(token, ParticipantInfo(conversationId, participantId))
            if (response.isSuccessful) {
                RepositoryResult.Success()
            } else {
                RepositoryResult.Error(message = response.errorBody()?.string())
            }
        } catch (e: Exception) {
            RepositoryResult.Error(message = e.message, throwable = e)
        }
    }

    suspend fun removeParticipant(conversationId: String, participantId: String): RepositoryResult<Nothing> {
        return try {
            val token = runCatching {
                tokenManager.getAccessToken()!!
            }.onFailure {
                return RepositoryResult.Error()
            }.getOrThrow()
            val response = service.removeParticipant(token, ParticipantInfo(conversationId, participantId))
            if (response.isSuccessful) {
                RepositoryResult.Success()
            } else {
                RepositoryResult.Error(message = response.errorBody()?.string())
            }
        } catch (e: Exception) {
            RepositoryResult.Error(message = e.message, throwable = e)
        }
    }

    suspend fun deleteConversation(conversationId: String): RepositoryResult<Nothing> {
        return try {
            val token = runCatching {
                tokenManager.getAccessToken()!!
            }.onFailure {
                return RepositoryResult.Error()
            }.getOrThrow()
            val response = service.deleteConversation(token, conversationId)
            if (response.isSuccessful) {
                RepositoryResult.Success()
            } else {
                RepositoryResult.Error(message = response.errorBody()?.string())
            }
        } catch (e: Exception) {
            RepositoryResult.Error(message = e.message, throwable = e)
        }
    }
}