package com.github.huymaster.textguardian.android.ui.state

sealed interface MainUiState {
    object Loading : MainUiState
    object ValidatingSession : MainUiState
    data class Success(val data: String) : MainUiState
    data class Error(val message: String) : MainUiState
}