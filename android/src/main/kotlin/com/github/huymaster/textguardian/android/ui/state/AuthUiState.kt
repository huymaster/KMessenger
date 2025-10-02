package com.github.huymaster.textguardian.android.ui.state

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorPhoneField: String? = null,
    val errorPasswordField: String? = null,
    val errorConfirmPasswordField: String? = null,
    val errorMessage: String? = null,
    val isLoginSuccess: Boolean = false,
    val isRegisterSuccess: Boolean = false
)