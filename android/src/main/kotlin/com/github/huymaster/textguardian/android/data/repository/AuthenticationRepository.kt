package com.github.huymaster.textguardian.android.data.repository

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.github.huymaster.textguardian.android.app.JWTTokenManager
import com.github.huymaster.textguardian.android.data.type.RepositoryResult
import com.github.huymaster.textguardian.core.api.APIVersion1Service
import com.github.huymaster.textguardian.core.api.type.LoginRequest
import com.github.huymaster.textguardian.core.api.type.RegisterRequest
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

sealed class AuthResult {
    data class Success(val message: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthenticationRepository(
    private val service: APIVersion1Service,
    private val tokenManager: JWTTokenManager
) : KoinComponent {
    suspend fun login(phone: String, password: String): RepositoryResult<Nothing> {
        return try {
            val response =
                service.login(LoginRequest(phone, password, getDeviceInfo()))
            if (response.isSuccessful) {
                val token = response.body()!!.refreshToken
                tokenManager.saveRefreshToken(token)
                RepositoryResult.Success(message = "Login successfully")
            } else {
                if (response.code() == 502) RepositoryResult.Error(message = "Service is not available")
                else
                    RepositoryResult.Error(
                        message = response.errorBody()?.string() ?: "Failed to login: ${response.code()}"
                    )
            }
        } catch (e: Exception) {
            RepositoryResult.Error(message = "Login failed. Can't connect to server [${e.javaClass.simpleName}]")
        }
    }

    private fun getDeviceInfo(): String {
        val manufacturer = Build.MANUFACTURER
        val androidVersion = Build.VERSION.RELEASE ?: "<?>"

        val user = Settings.Global.getString(get<Context>().contentResolver, Settings.Global.DEVICE_NAME)
        val model = Build.MODEL

        return StringBuilder()
            .append(manufacturer).append(" ")
            .append(user ?: model ?: "Unknown").append(" ")
            .append("(Android $androidVersion)")
            .toString()
    }

    suspend fun register(phone: String, password: String): RepositoryResult<Nothing> {
        return try {
            val response = service.register(RegisterRequest(phone, password))
            if (response.isSuccessful) {
                RepositoryResult.Success(message = "Register successfully")
            } else {
                if (response.code() == 502) RepositoryResult.Error(message = "Service is not available")
                else
                    RepositoryResult.Error(
                        message = response.errorBody()?.string() ?: "Failed to register: ${response.code()}"
                    )
            }
        } catch (e: Exception) {
            RepositoryResult.Error(message = "Register failed: Can't connect to server [${e.javaClass.simpleName}]")
        }
    }
}