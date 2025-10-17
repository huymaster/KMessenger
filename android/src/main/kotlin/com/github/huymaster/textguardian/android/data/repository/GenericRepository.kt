package com.github.huymaster.textguardian.android.data.repository

import com.github.huymaster.textguardian.core.api.APIBase
import kotlinx.coroutines.CancellationException

sealed class ServiceHealth {
    object Healthy : ServiceHealth()
    data class Unhealthy(val message: String) : ServiceHealth()
}

class GenericRepository(
    private val service: APIBase
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
            ServiceHealth.Unhealthy("Check service health failed: ${e.javaClass.simpleName}")
        }
    }
}