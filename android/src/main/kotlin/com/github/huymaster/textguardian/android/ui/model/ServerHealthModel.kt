package com.github.huymaster.textguardian.android.ui.model

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.huymaster.textguardian.android.data.repository.AppSettings
import com.github.huymaster.textguardian.android.data.repository.AppSettingsManager
import com.github.huymaster.textguardian.android.ui.state.MainUiState
import com.github.huymaster.textguardian.core.api.APIBase
import com.github.huymaster.textguardian.core.api.APIVersion1Service
import com.github.huymaster.textguardian.core.api.type.RefreshToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class ServerHealthModel : ViewModel() {
    companion object {
        private const val TAG = "ServerHealthModel"
    }

    private val apiBase by inject<APIBase>(APIBase::class.java)
    private val apiVersion1 by inject<APIVersion1Service>(APIVersion1Service::class.java)
    private val settings by inject<AppSettingsManager>(AppSettingsManager::class.java)
    private val _serverHealthState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val serverHealthState = _serverHealthState.asStateFlow()

    var isSessionValid by mutableStateOf(false)
        private set

    private suspend fun <T> retryIO(times: Int = 3, block: suspend () -> T): T {
        repeat(times - 1) {
            try {
                return block()
            } catch (e: Exception) {
                delay(1000L)
            }
        }
        return block()
    }

    fun getServerHealth() {
        viewModelScope.launch {
            _serverHealthState.value = MainUiState.Loading
            try {
                val result = retryIO { apiBase.health() }
                if (!result.isSuccessful)
                    _serverHealthState.value = MainUiState.Error("${result.code()}: Failed connect to server")
                else
                    validateSession()
            } catch (e: Exception) {
                _serverHealthState.value = MainUiState.Error("Can't connect to server")
                Log.w(TAG, e)
            }
        }
    }

    fun validateSession() {
        viewModelScope.launch {
            if (settings.get(AppSettings.REFRESH_TOKEN).isEmpty()) {
                _serverHealthState.value = MainUiState.Success("")
                return@launch
            }
            _serverHealthState.value = MainUiState.ValidatingSession
            try {
                val refreshToken = settings.get(AppSettings.REFRESH_TOKEN)
                if (refreshToken.isEmpty()) {
                    _serverHealthState.value = MainUiState.Success("")
                    return@launch
                }
                val result = retryIO { apiVersion1.checkSession(RefreshToken(refreshToken)) }
                isSessionValid = result.isSuccessful
                if (result.isSuccessful || result.code() == 401) {
                    if (result.code() == 401)
                        settings.remove(AppSettings.REFRESH_TOKEN)
                    _serverHealthState.value = MainUiState.Success("")
                } else
                    _serverHealthState.value = MainUiState.Error("${result.code()}: Failed to validate session")
            } catch (e: Exception) {
                _serverHealthState.value = MainUiState.Error("Can't validate session")
                Log.w(TAG, e)
            }
        }
    }
}