package com.github.huymaster.textguardian.android.app

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.LifecycleCoroutineScope
import com.github.huymaster.textguardian.core.api.APIVersion1Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.time.temporal.ChronoUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class JWTTokenManager(context: Context) : KoinComponent {
    private val preference: SharedPreferences = context.getSharedPreferences("token_storage", Context.MODE_PRIVATE)
    private val mutex = Mutex()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val refreshToken = "token"
    private val expireTime = 10L
    private val expireUnit = ChronoUnit.MINUTES
    private var lastToken: String? = null

    private fun getRefreshToken(): String? = preference.getString(refreshToken, null)

    fun saveRefreshToken(token: String) {
        preference.edit(commit = true) { putString(refreshToken, token) }
        ApplicationEvents().resetSessionExpired()
    }

    fun removeRefreshToken() {
        preference.edit(commit = true) { remove(refreshToken) }
        ApplicationEvents().notifySessionExpired()
    }

    fun autoCheckToken(
        scope: LifecycleCoroutineScope,
        interval: Duration = 30.seconds
    ) {
        if (interval < 10.seconds)
            Log.w("JWTTokenManager", "Interval must be at least 10 seconds")
        scope.launch {
            while (true) {
                launch { runCatching { getAccessToken() } }
                delay(if (interval < 10.seconds) 10.seconds else interval)
            }
        }
    }

    suspend fun getAccessToken(service: APIVersion1Service = get()): String? {
        mutex.withLock {
            if (lastToken == null || lastToken?.let { service.check(it) }?.isSuccessful != true)
                return requireNewAccessToken(service)
            return lastToken
        }
    }

    suspend fun requireNewAccessToken(service: APIVersion1Service = get()): String {
        val refreshToken = getRefreshToken()
        if (refreshToken == null) {
            ApplicationEvents().notifySessionExpired()
            throw IllegalStateException("Refresh token not found")
        }
        val response = service.refresh(refreshToken)
        if (response.isSuccessful) {
            val body = response.body() ?: throw IllegalStateException("Failed to get token. Error ${response.code()}")
            lastToken = body.accessToken
            return body.accessToken
        } else {
            if (response.code() == 401 || response.code() == 403) {
                lastToken = null
                removeRefreshToken()
            }
            throw IllegalStateException("Failed to get token. Error ${response.code()}")
        }
    }
}