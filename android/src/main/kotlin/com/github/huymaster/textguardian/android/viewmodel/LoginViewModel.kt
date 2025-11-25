package com.github.huymaster.textguardian.android.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.huymaster.textguardian.android.app.CipherManager
import com.github.huymaster.textguardian.android.data.repository.AuthenticationRepository
import com.github.huymaster.textguardian.android.data.repository.CipherRepository
import com.github.huymaster.textguardian.android.data.type.RepositoryResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.inject

data class LoginState(
    val isLoading: Boolean = false,
    val phoneError: String? = null,
    val passwordError: String? = null,
    val message: String? = null,
    val isSuccess: Boolean = false
)

class LoginViewModel : BaseViewModel() {
    private val repository: AuthenticationRepository by inject()
    private val cRepository: CipherRepository by inject()
    private val manager: CipherManager by inject()
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val phonePattern = Regex("^0\\d{9}$")
    private val passwordPattern = Regex("^[\\w_]{6,32}$")

    var phone by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set

    fun updatePhone(phone: String) {
        this.phone = phone.trim()
        validatePhone()
    }

    fun updatePassword(password: String) {
        this.password = password.trim()
        validatePassword()
    }

    fun resetState() {
        _state.value = LoginState()
        phone = ""
        password = ""
    }

    private fun validatePhone() {
        if (!phonePattern.matches(phone) && phone.isNotEmpty())
            _state.value = _state.value.copy(phoneError = "Invalid phone number")
        else
            _state.value = _state.value.copy(phoneError = null)
    }

    private fun validatePassword() {
        if (password.length !in 6..32 && password.isNotEmpty())
            _state.value = _state.value.copy(passwordError = "Password must be between 6 and 32 characters long")
        else if (!passwordPattern.matches(password) && password.isNotEmpty())
            _state.value = _state.value.copy(passwordError = "Invalid password (only word, number and underscore '_')")
        else
            _state.value = _state.value.copy(passwordError = null)
    }

    suspend fun login() {
        _state.value = _state.value.copy(isLoading = true)
        when (val result = repository.login(phone, password)) {
            is RepositoryResult.Success -> {
                resetState()
                manager.generateNewKeyPair()
                cRepository.sendPublicKey()

                _state.value = _state.value.copy(isLoading = false, isSuccess = true, message = result.message)
            }

            is RepositoryResult.Error ->
                _state.value = _state.value.copy(isLoading = false, isSuccess = false, message = result.message)

            else -> _state.value = _state.value.copy(isLoading = false, isSuccess = false, message = "Unknown error")
        }
    }
}