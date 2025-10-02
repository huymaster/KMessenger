package com.github.huymaster.textguardian.android.ui.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.github.huymaster.textguardian.android.ui.model.AuthenticationViewModel

@Composable
fun RegisterScreen(
    model: AuthenticationViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val state by model.authUiState.collectAsState()
    var isHidePassword by remember { mutableStateOf(true) }
    val icon = if (isHidePassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            enabled = !state.isLoading,
            label = { Text("Phone number") },
            placeholder = { Text("Enter your phone number") },
            value = model.phoneNumber,
            onValueChange = model::onPhoneNumberChange,
            isError = state.errorPhoneField != null,
            supportingText = { Text(state.errorPhoneField ?: "") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            enabled = !state.isLoading,
            label = { Text("Password") },
            placeholder = { Text("Enter your password") },
            value = model.password,
            onValueChange = model::onPasswordChange,
            isError = state.errorPasswordField != null,
            supportingText = { Text(state.errorPasswordField ?: "") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                Crossfade(isHidePassword) {
                    IconButton(onClick = { isHidePassword = !isHidePassword }) { Icon(icon, null) }
                }
            },
            visualTransformation = if (isHidePassword) PasswordVisualTransformation() else VisualTransformation.None
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            enabled = !state.isLoading,
            label = { Text("Confirm password") },
            placeholder = { Text("Re-enter your password") },
            value = model.confirmPassword,
            onValueChange = model::onConfirmPasswordChange,
            isError = state.errorConfirmPasswordField != null,
            supportingText = { Text(state.errorConfirmPasswordField ?: "") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation()
        )
        Text(state.errorMessage ?: "", color = MaterialTheme.colorScheme.error)
        if (state.isRegisterSuccess)
            Text("Register success", color = MaterialTheme.colorScheme.primary)
        Button(
            enabled = !state.isLoading &&
                    state.errorPhoneField == null &&
                    state.errorPasswordField == null &&
                    state.errorConfirmPasswordField == null &&
                    model.phoneNumber.isNotEmpty() &&
                    model.password.isNotEmpty() &&
                    model.confirmPassword.isNotEmpty(),
            onClick = model::register
        ) { Text("Register") }
    }
}