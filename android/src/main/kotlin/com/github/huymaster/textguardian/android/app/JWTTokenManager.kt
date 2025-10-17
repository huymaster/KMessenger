package com.github.huymaster.textguardian.android.app

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.github.huymaster.textguardian.core.api.APIVersion1Service
import com.github.huymaster.textguardian.core.api.type.RefreshToken
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.time.temporal.ChronoUnit

class JWTTokenManager(context: Context) {
    private val preference: SharedPreferences = context.getSharedPreferences("token_storage", Context.MODE_PRIVATE)
    private val mutex = Mutex()
    private val refreshToken = "token"
    private val expireTime = 10L
    private val expireUnit = ChronoUnit.MINUTES
    private var lastTokenFetch: Instant = Instant.EPOCH
    private var lastToken: String? = null

    private fun getRefreshToken(): String? = preference.getString(refreshToken, null)

    fun saveRefreshToken(token: String) {
        preference.edit { putString(refreshToken, token) }
    }

    suspend fun verify(service: APIVersion1Service): Boolean {
        val refreshToken = getRefreshToken() ?: return false
        val tokenValid = service.checkSession(RefreshToken(refreshToken)).isSuccessful
        if (!tokenValid) removeRefreshToken()
        return tokenValid
    }

    fun removeRefreshToken() {
        preference.edit { remove(refreshToken) }
    }

    suspend fun getAccessToken(service: APIVersion1Service): String {
        mutex.withLock {
            val expire = lastTokenFetch.plus(expireTime, expireUnit)
            if (lastToken == null || Instant.now().isAfter(expire))
                return requireNewAccessToken(service)
            return lastToken!!
        }
    }

    suspend fun requireNewAccessToken(service: APIVersion1Service): String {
        val accessToken = service.refreshToken(RefreshToken(getRefreshToken()!!))
        if (accessToken.isSuccessful) {
            lastTokenFetch = Instant.now()
            return accessToken.body()!!.accessToken.also { lastToken = it }
        } else {
            removeRefreshToken()
            throw IllegalStateException("Failed to get token. Error ${accessToken.code()}")
        }
    }
}