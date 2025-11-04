package com.github.huymaster.textguardian.android.viewmodel

import com.github.huymaster.textguardian.android.app.JWTTokenManager
import com.github.huymaster.textguardian.android.data.repository.GenericRepository
import com.github.huymaster.textguardian.android.data.type.RepositoryResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.inject

data class InitState(
    val isLoading: Boolean = true,
    val isServiceAvailable: Boolean = false,
    val error: String? = null
)

class InitViewModel : BaseViewModel() {
    private val repository: GenericRepository by inject()
    private val tokenManager: JWTTokenManager by inject()
    private val _state = MutableStateFlow(InitState())
    val state: StateFlow<InitState> = _state.asStateFlow()

    suspend fun checkServiceHealth() {
        _state.value = _state.value.copy(isLoading = true)
        try {
            when (val health = repository.checkServiceHealth()) {
                is RepositoryResult.Success -> _state.value =
                    _state.value.copy(isLoading = false, isServiceAvailable = true)

                is RepositoryResult.Error -> _state.value =
                    _state.value.copy(isLoading = false, error = health.message)

                else -> _state.value = _state.value.copy(isLoading = false, error = "Unknown error")
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false, error = e.message)
        }
    }

    suspend fun validateSession(): Boolean {
        return runCatching { tokenManager.getAccessToken() }.isSuccess
    }
}