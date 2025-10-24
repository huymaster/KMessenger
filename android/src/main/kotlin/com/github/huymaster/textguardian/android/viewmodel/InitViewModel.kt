package com.github.huymaster.textguardian.android.viewmodel

import androidx.lifecycle.ViewModel
import com.github.huymaster.textguardian.android.app.JWTTokenManager
import com.github.huymaster.textguardian.android.data.repository.GenericRepository
import com.github.huymaster.textguardian.android.data.repository.ServiceHealth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.java.KoinJavaComponent.get

data class InitState(
    val isLoading: Boolean = true,
    val isServiceAvailable: Boolean = false,
    val error: String? = null
)

class InitViewModel(
    private val repository: GenericRepository = get(GenericRepository::class.java),
    private val tokenManager: JWTTokenManager = get(JWTTokenManager::class.java)
) : ViewModel() {
    private val _state = MutableStateFlow(InitState())
    val state: StateFlow<InitState> = _state.asStateFlow()

    suspend fun checkServiceHealth() {
        _state.value = _state.value.copy(isLoading = true)
        try {
            when (val health = repository.checkServiceHealth()) {
                is ServiceHealth.Healthy -> _state.value =
                    _state.value.copy(isLoading = false, isServiceAvailable = true)

                is ServiceHealth.Unhealthy -> _state.value =
                    _state.value.copy(isLoading = false, error = health.message)
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false, error = e.message)
        }
    }

    suspend fun validateSession(): Boolean {
        return runCatching { tokenManager.getAccessToken() }.isSuccess
    }
}