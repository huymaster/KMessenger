package com.github.huymaster.textguardian.android.data.repository

import android.util.Log
import com.github.huymaster.textguardian.core.api.APIVersion1Service
import kotlinx.coroutines.CancellationException

sealed class ServiceHealth {
    object Healthy : ServiceHealth()
    data class Unhealthy(val message: String) : ServiceHealth()
}

class GenericRepository(
    private val service: APIVersion1Service
) {
    suspend fun checkServiceHealth(): ServiceHealth {
        return try {
            val response = service.health()
            if (response.isSuccessful) {
                ServiceHealth.Healthy
            } else {
                if (response.code() == 502)
                    ServiceHealth.Unhealthy("Service is not available")
                else
                    ServiceHealth.Unhealthy(response.errorBody()?.string() ?: "Connection error: ${response.code()}")
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.wtf(null, e)
            ServiceHealth.Unhealthy("Check service health failed: ${e.javaClass.simpleName}.\nPlease check your internet connection")
        }
    }
}