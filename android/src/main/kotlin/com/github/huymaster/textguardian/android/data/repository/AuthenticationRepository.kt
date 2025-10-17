package com.github.huymaster.textguardian.android.data.repository

import com.github.huymaster.textguardian.android.app.JWTTokenManager
import com.github.huymaster.textguardian.core.api.APIVersion1Service
import com.github.huymaster.textguardian.core.api.type.LoginRequest
import com.github.huymaster.textguardian.core.api.type.RegisterRequest

sealed class AuthResult {
    data class Success(val message: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthenticationRepository(
    private val service: APIVersion1Service,
    private val tokenManager: JWTTokenManager
) {
    suspend fun login(phone: String, password: String): AuthResult {
        return try {
            val response = service.login(LoginRequest(phone, password))
            if (response.isSuccessful) {
                val token = response.body()!!.refreshToken
                tokenManager.saveRefreshToken(token)
                AuthResult.Success("Login successfully")
            } else {
                if (response.code() == 502) AuthResult.Error("Service is not available")
                else
                    AuthResult.Error(response.errorBody()?.string() ?: "Failed to login: ${response.code()}")
            }
        } catch (e: Exception) {
            AuthResult.Error("Login failed. Can't connect to server [${e.javaClass.simpleName}]")
        }
    }

    suspend fun register(phone: String, password: String): AuthResult {
        return try {
            val response = service.register(RegisterRequest(phone, password))
            if (response.isSuccessful) {
                AuthResult.Success("Register successfully")
            } else {
                if (response.code() == 502) AuthResult.Error("Service is not available")
                else
                    AuthResult.Error(response.errorBody()?.string() ?: "Failed to register: ${response.code()}")
            }
        } catch (e: Exception) {
            AuthResult.Error("Register failed: Can't connect to server [${e.javaClass.simpleName}]")
        }
    }
}