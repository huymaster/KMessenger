package com.github.huymaster.textguardian.android.data.repository

import com.github.huymaster.textguardian.android.app.CipherManager
import com.github.huymaster.textguardian.android.app.JWTTokenManager
import com.github.huymaster.textguardian.android.data.type.RepositoryResult
import com.github.huymaster.textguardian.core.api.APIVersion1Service
import com.github.huymaster.textguardian.core.api.type.UserPublicKey
import com.github.huymaster.textguardian.core.api.type.UserPublicKeys
import org.koin.core.component.KoinComponent

class CipherRepository(
    private val service: APIVersion1Service,
    private val tokenManager: JWTTokenManager,
    private val cipherManager: CipherManager
) : KoinComponent {
    suspend fun sendPublicKey(): RepositoryResult<Nothing> {
        val token = tokenManager.getAccessToken() ?: return RepositoryResult.Error(message = "Token is null")

        return runCatching {
            val publicKey = UserPublicKey(null, key = cipherManager.publicKey)
            service.addPublicKey(token, publicKey)
        }.fold(
            onSuccess = { RepositoryResult.Success() },
            onFailure = { RepositoryResult.Error(it) }
        )
    }

    suspend fun getPublicKey(userId: String): RepositoryResult<UserPublicKeys> {
        return runCatching {
            service.getPublicKeys(userId).body()!!
        }.fold(
            onSuccess = { RepositoryResult.Success(it) },
            onFailure = { RepositoryResult.Error(it, it.message) }
        )
    }
}