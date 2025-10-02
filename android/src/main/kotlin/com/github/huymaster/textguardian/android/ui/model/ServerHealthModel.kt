package com.github.huymaster.textguardian.android.ui.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.huymaster.textguardian.android.ui.state.MainUiState
import com.github.huymaster.textguardian.core.api.APIBase
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
    private val _serverHealthState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val serverHealthState = _serverHealthState.asStateFlow()
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
                _serverHealthState.value = if (result.isSuccessful)
                    MainUiState.Success(result.body()!!)
                else
                    MainUiState.Error("${result.code()}: Failed connect to server")

            } catch (e: Exception) {
                _serverHealthState.value = MainUiState.Error("Can't connect to server")
                Log.w(TAG, e)
            }
        }
    }
}