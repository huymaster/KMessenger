package com.github.huymaster.textguardian.android.data.repository

import com.github.huymaster.textguardian.android.app.JWTTokenManager
import com.github.huymaster.textguardian.android.data.type.RepositoryResult
import com.github.huymaster.textguardian.core.api.APIVersion1Service
import com.github.huymaster.textguardian.core.api.type.UserInfo
import java.util.*

class UserRepository(
    private val service: APIVersion1Service,
    private val tokenManager: JWTTokenManager
) {
    suspend fun getMe(): RepositoryResult<UserInfo> = getUserInfo(null)

    suspend fun getUserInfo(uuid: UUID?): RepositoryResult<UserInfo> {
        return try {
            val token = runCatching {
                tokenManager.getAccessToken()!!
            }.onFailure {
                return RepositoryResult.Error(throwable = it)
            }.getOrThrow()
            val response = service.getUserInfo(token, uuid?.toString())
            if (response.isSuccessful) {
                RepositoryResult.Success(response.body())
            } else {
                RepositoryResult.Error(message = response.errorBody()?.string())
            }
        } catch (e: Exception) {
            RepositoryResult.Error(message = e.message, throwable = e)
        }
    }

    suspend fun findUsers(query: String): RepositoryResult<List<UserInfo>> {
        return try {
            val response = service.findUsers(query, query)
            if (response.isSuccessful) {
                RepositoryResult.Success(response.body())
            } else {
                RepositoryResult.Error(message = response.errorBody()?.string())
            }
        } catch (e: Exception) {
            RepositoryResult.Error(message = e.message, throwable = e)
        }
    }
}