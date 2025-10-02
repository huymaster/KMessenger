package com.github.huymaster.textguardian.android.ui.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.huymaster.textguardian.android.ui.state.AuthUiState
import com.github.huymaster.textguardian.core.api.APIVersion1Service
import com.github.huymaster.textguardian.core.api.type.LoginRequest
import com.github.huymaster.textguardian.core.api.type.RegisterRequest
import com.github.huymaster.textguardian.core.utils.createService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthenticationViewModel : ViewModel() {
    private val _authUiState = MutableStateFlow(AuthUiState())
    private val service = createService(APIVersion1Service::class)
    private val phonePattern = Regex("^0\\d{9}$")
    private val passwordPattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,}$")
    val authUiState = _authUiState.asStateFlow()
    var allowSwitchMode by mutableStateOf(true)
    var phoneNumber by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    fun toggleMode() {
        onPhoneNumberChange("")
        onPasswordChange("")
        onConfirmPasswordChange("")
        _authUiState.update { AuthUiState() }
    }

    fun onPhoneNumberChange(number: String) {
        if (!number.matches(phonePattern) && number.isNotEmpty())
            _authUiState.update { it.copy(errorPhoneField = "Phone number must be 10 digits and start with 0") }
        else
            _authUiState.update { it.copy(errorPhoneField = null) }
        phoneNumber = number
    }

    fun onPasswordChange(pwd: String) {
        if (!pwd.matches(passwordPattern) && pwd.isNotEmpty())
            _authUiState.update { it.copy(errorPasswordField = "Password must 8 characters and must be [A-Z][a-z][0-9]") }
        else
            _authUiState.update { it.copy(errorPasswordField = null) }
        password = pwd
        onConfirmPasswordChange(confirmPassword)
    }

    fun onConfirmPasswordChange(pwd: String) {
        if (password != pwd && pwd.isNotEmpty())
            _authUiState.update { it.copy(errorConfirmPasswordField = "Password not match") }
        else
            _authUiState.update { it.copy(errorConfirmPasswordField = null) }
        confirmPassword = pwd
    }

    fun login() {
        if (phoneNumber.isEmpty() || password.isEmpty())
            _authUiState.update { it.copy(errorMessage = "Please fill all fields") }
        else
            _authUiState.update { it.copy(errorMessage = null) }
        val request = LoginRequest(phoneNumber, password)
        runInLoading {
            val response = service.login(request)
            if (!response.isSuccessful) {
                val error = if (response.code() == 502)
                    "Can't connect to server"
                else
                    response.errorBody()?.string()
                _authUiState.update {
                    it.copy(
                        isLoginSuccess = false,
                        errorMessage = error ?: "Failed to login"
                    )
                }
            } else {
                _authUiState.update {
                    it.copy(
                        isLoginSuccess = true,
                        errorMessage = null
                    )
                }
                onPhoneNumberChange("")
                onPasswordChange("")
            }
        }
    }

    fun register() {
        if (phoneNumber.isEmpty() || password.isEmpty() || confirmPassword.isEmpty())
            _authUiState.update { it.copy(errorMessage = "Please fill all fields") }
        else
            _authUiState.update { it.copy(errorMessage = null) }
        val request = RegisterRequest(phoneNumber, password)
        runInLoading {
            val response = service.register(request)
            if (!response.isSuccessful) {
                val error = if (response.code() == 502)
                    "Can't connect to server"
                else
                    response.errorBody()?.string()
                _authUiState.update {
                    it.copy(
                        isRegisterSuccess = false,
                        errorMessage = error ?: "Failed to register"
                    )
                }
            } else {
                _authUiState.update {
                    it.copy(
                        isRegisterSuccess = true,
                        errorMessage = null
                    )
                }
                onPhoneNumberChange("")
                onPasswordChange("")
                onConfirmPasswordChange("")
            }
        }
    }

    private fun runInLoading(block: suspend CoroutineScope.() -> Unit) =
        viewModelScope.launch(block = {
            allowSwitchMode = false
            _authUiState.update { it.copy(isLoading = true) }
            block()
            _authUiState.update { it.copy(isLoading = false) }
            allowSwitchMode = true
        })
}