package com.github.huymaster.textguardian.android.app

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.github.huymaster.textguardian.core.api.APIVersion1Service
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.time.Instant
import java.time.temporal.ChronoUnit

class JWTTokenManager(context: Context) : KoinComponent {
    private val preference: SharedPreferences = context.getSharedPreferences("token_storage", Context.MODE_PRIVATE)
    private val mutex = Mutex()
    private val refreshToken = "token"
    private val expireTime = 30L
    private val expireUnit = ChronoUnit.MINUTES
    private var lastTokenFetch: Instant = Instant.EPOCH
    private var lastToken: String? = null

    private fun getRefreshToken(): String? = preference.getString(refreshToken, null)

    fun saveRefreshToken(token: String) {
        preference.edit { putString(refreshToken, token) }
    }

    fun removeRefreshToken() {
        preference.edit { remove(refreshToken) }
    }

    suspend fun getAccessToken(service: APIVersion1Service = get()): String {
        mutex.withLock {
            val expire = lastTokenFetch.plus(expireTime, expireUnit)
            if (lastToken == null || Instant.now().isAfter(expire))
                return requireNewAccessToken(service)
            return lastToken!!
        }
    }

    suspend fun requireNewAccessToken(service: APIVersion1Service = get()): String {
        val refreshToken = getRefreshToken() ?: throw IllegalStateException("Refresh token not found")
        val response = service.refresh(refreshToken)
        if (response.isSuccessful) {
            val body = response.body() ?: throw IllegalStateException("Failed to get token. Error ${response.code()}")
            lastTokenFetch = Instant.now()
            lastToken = body.accessToken
            return body.accessToken
        } else {
            if (response.code() == 401)
                removeRefreshToken()
            throw IllegalStateException("Failed to get token. Error ${response.code()}")
        }
    }
}