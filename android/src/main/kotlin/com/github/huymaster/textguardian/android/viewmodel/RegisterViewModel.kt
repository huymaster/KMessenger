package com.github.huymaster.textguardian.android.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.github.huymaster.textguardian.android.data.repository.AuthResult
import com.github.huymaster.textguardian.android.data.repository.AuthenticationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.java.KoinJavaComponent.get


data class RegisterState(
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val phoneError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val message: String? = null,
    val isSuccess: Boolean = false
)

class RegisterViewModel(
    private val repository: AuthenticationRepository = get(AuthenticationRepository::class.java)
) : ViewModel() {
    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    private val emailPattern = Regex("^[\\w.]+@\\w+\\.[\\w.]+$")
    private val phonePattern = Regex("^0\\d{9}$")
    private val passwordPattern = Regex("^[\\w_]{6,32}$")

    var email by mutableStateOf("")
        private set
    var phone by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("")
        private set

    fun updateEmail(email: String) {
        this.email = email.trim()
        validateEmail()
    }

    fun updatePhone(phone: String) {
        this.phone = phone.trim()
        validatePhone()
    }

    fun updatePassword(password: String) {
        this.password = password.trim()
        validatePassword()
        validateConfirmPassword()
    }

    fun updateConfirmPassword(confirmPassword: String) {
        this.confirmPassword = confirmPassword.trim()
        validatePassword()
        validateConfirmPassword()
    }

    fun resetState() {
        _state.value = RegisterState()
        email = ""
        phone = ""
        password = ""
        confirmPassword = ""
    }

    private fun validateEmail() {
        if (!emailPattern.matches(email) && email.isNotEmpty())
            _state.value = _state.value.copy(emailError = "Invalid email address")
        else
            _state.value = _state.value.copy(emailError = null)
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

    private fun validateConfirmPassword() {
        if (confirmPassword != password && confirmPassword.isNotEmpty())
            _state.value = _state.value.copy(confirmPasswordError = "Passwords do not match")
        else
            _state.value = _state.value.copy(confirmPasswordError = null)
    }

    suspend fun register() {
        _state.update { it.copy(isLoading = true) }
        when (val result = repository.register(phone, password)) {
            is AuthResult.Success -> {
                resetState()
                _state.update { it.copy(isLoading = false, isSuccess = true, message = result.message) }
            }

            is AuthResult.Error -> {
                _state.update { it.copy(isLoading = false, isSuccess = false, message = result.message) }
            }
        }
    }
}