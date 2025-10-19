@file:OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalLayoutApi::class,
    ExperimentalSharedTransitionApi::class
)

package com.github.huymaster.textguardian.android.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.huymaster.textguardian.android.viewmodel.LoginState
import com.github.huymaster.textguardian.android.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    model: LoginViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onSwitchToRegister: () -> Unit = {},
    onLoginSuccess: () -> Unit
) {
    val state by model.state.collectAsState()
    val scope = rememberCoroutineScope()
    LaunchedEffect(state) {
        if (state.isSuccess) onLoginSuccess()
    }
    with(sharedTransitionScope) {
        Box(
            modifier = Modifier
                .imePadding()
                .fillMaxSize()
                .padding(16.dp, 32.dp, 16.dp, 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .sharedBounds(
                        rememberSharedContentState("bound"),
                        animatedContentScope,
                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                    )
                    .border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.shapes.large)
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                LoginScreenContent(
                    state = state,
                    phoneValue = model.phone,
                    passwordValue = model.password,
                    onPhoneChange = model::updatePhone,
                    onPasswordChange = model::updatePassword,
                    animatedContentScope = animatedContentScope,
                    onSwitchToRegister = onSwitchToRegister,
                    onLoginClick = { scope.launch { model.login() } }
                )
            }
        }
    }
}

@Composable
private fun SharedTransitionScope.LoginScreenContent(
    state: LoginState,
    phoneValue: String,
    passwordValue: String,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    animatedContentScope: AnimatedContentScope,
    onSwitchToRegister: () -> Unit = {},
    onLoginClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val passwordFocus = remember { FocusRequester() }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            "Login",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(16.dp)
                .sharedElement(rememberSharedContentState("title"), animatedContentScope)
                .fillMaxWidth()
        )
        OutlinedTextField(
            value = phoneValue,
            onValueChange = onPhoneChange,
            label = { Text("Phone number") },
            leadingIcon = { Icon(Icons.Default.Phone, null) },
            singleLine = true,
            isError = state.phoneError != null,
            supportingText = { Text(state.phoneError ?: "") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
            modifier = Modifier
                .fillMaxWidth()
                .sharedElement(rememberSharedContentState("phone"), animatedContentScope)
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp)
        )
        OutlinedTextField(
            value = passwordValue,
            onValueChange = onPasswordChange,
            leadingIcon = { Icon(Icons.Default.Password, null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                }
            },
            label = { Text("Password") },
            singleLine = true,
            isError = state.passwordError != null,
            supportingText = { Text(state.passwordError ?: "") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onLoginClick() }),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier
                .focusRequester(passwordFocus)
                .fillMaxWidth()
                .sharedElement(rememberSharedContentState("password"), animatedContentScope)
                .padding(horizontal = 16.dp)
        )
        Text(
            text = state.message ?: "",
            color = if (state.isSuccess)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.error
        )
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Button(
                enabled = state.phoneError == null &&
                        state.passwordError == null &&
                        phoneValue.isNotEmpty() &&
                        passwordValue.isNotEmpty() &&
                        !state.isLoading,
                onClick = onLoginClick,
                modifier = Modifier
                    .sharedElement(rememberSharedContentState("button"), animatedContentScope)
                    .skipToLookaheadSize()
            ) {
                AnimatedContent(
                    state.isLoading
                ) {
                    if (it) CircularProgressIndicator(Modifier.size(24.dp))
                    else Text("Login", modifier = Modifier)
                }
            }
        }
        TextButton(
            modifier = Modifier
                .align(Alignment.End)
                .sharedElement(rememberSharedContentState("switch"), animatedContentScope),
            onClick = onSwitchToRegister
        ) {
            Text("Don't have an account? Register", modifier = Modifier.skipToLookaheadSize())
        }
    }
}