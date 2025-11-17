package com.github.huymaster.textguardian.android.data.repository

import android.util.Log
import com.github.huymaster.textguardian.android.data.type.RepositoryResult
import com.github.huymaster.textguardian.core.api.APIVersion1Service
import kotlinx.coroutines.CancellationException
import org.koin.core.component.KoinComponent

class GenericRepository(
    private val service: APIVersion1Service
) : KoinComponent {
    suspend fun checkServiceHealth(): RepositoryResult<Nothing> {
        return try {
            val response = service.health()
            if (response.isSuccessful) {
                RepositoryResult.Success()
            } else {
                if (response.code() == 502)
                    RepositoryResult.Error(message = "Service is not available")
                else
                    RepositoryResult.Error(
                        message = response.errorBody()?.string() ?: "Connection error: ${response.code()}"
                    )
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.wtf(null, e)
            RepositoryResult.Error(message = "Check service health failed: ${e.javaClass.simpleName}.\nPlease check your internet connection")
        }
    }
}