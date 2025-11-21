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
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.huymaster.textguardian.android.viewmodel.RegisterState
import com.github.huymaster.textguardian.android.viewmodel.RegisterViewModel
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onSwitchToLogin: () -> Unit = {}
) {
    val model = viewModel<RegisterViewModel>()
    val state by model.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
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
                RegisterScreenContent(
                    state = state,
                    emailValue = model.email,
                    phoneValue = model.phone,
                    passwordValue = model.password,
                    confirmPasswordValue = model.confirmPassword,
                    onEmailChange = model::updateEmail,
                    onPhoneChange = model::updatePhone,
                    onPasswordChange = model::updatePassword,
                    onConfirmPasswordChange = model::updateConfirmPassword,
                    animatedContentScope = animatedContentScope,
                    onSwitchToLogin = onSwitchToLogin,
                    onRegisterClick = { scope.launch { model.register() } }
                )
            }
        }
    }
}

@Composable
private fun SharedTransitionScope.RegisterScreenContent(
    state: RegisterState,
    emailValue: String,
    phoneValue: String,
    passwordValue: String,
    confirmPasswordValue: String,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    animatedContentScope: AnimatedContentScope,
    onSwitchToLogin: () -> Unit = {},
    onRegisterClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    var passwordVisible by remember { mutableStateOf(false) }
    val phoneFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    val confirmPasswordFocus = remember { FocusRequester() }
    Column(
        modifier = Modifier
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            "Register",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(16.dp)
                .sharedElement(rememberSharedContentState("title"), animatedContentScope)
                .fillMaxWidth()
        )
        OutlinedTextField(
            value = emailValue,
            onValueChange = onEmailChange,
            label = { Text("Email (optional)") },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            singleLine = true,
            isError = state.emailError != null,
            supportingText = { Text(state.emailError ?: "") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { phoneFocus.requestFocus() }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp)
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
                .focusRequester(phoneFocus)
                .fillMaxWidth()
                .sharedElement(rememberSharedContentState("phone"), animatedContentScope)
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { confirmPasswordFocus.requestFocus() }),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier
                .focusRequester(passwordFocus)
                .fillMaxWidth()
                .sharedElement(rememberSharedContentState("password"), animatedContentScope)
                .padding(horizontal = 16.dp)
        )
        OutlinedTextField(
            value = confirmPasswordValue,
            onValueChange = onConfirmPasswordChange,
            leadingIcon = { Icon(Icons.Default.Pending, null) },
            label = { Text("Confirm password") },
            singleLine = true,
            isError = state.confirmPasswordError != null,
            supportingText = { Text(state.confirmPasswordError ?: "") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { confirmPasswordFocus.requestFocus() }),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier
                .focusRequester(confirmPasswordFocus)
                .fillMaxWidth()
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
                enabled = state.emailError == null &&
                        state.phoneError == null &&
                        state.passwordError == null &&
                        state.confirmPasswordError == null &&
                        phoneValue.isNotEmpty() &&
                        passwordValue.isNotEmpty() &&
                        confirmPasswordValue.isNotEmpty() &&
                        !state.isLoading,
                onClick = onRegisterClick,
                modifier = Modifier
                    .sharedElement(rememberSharedContentState("button"), animatedContentScope)
                    .skipToLookaheadSize()
            ) {
                AnimatedContent(
                    state.isLoading
                ) {
                    if (it) CircularProgressIndicator(Modifier.size(24.dp))
                    else Text("Register", modifier = Modifier.skipToLookaheadSize())
                }
            }
        }
        TextButton(
            modifier = Modifier
                .align(Alignment.End)
                .sharedElement(rememberSharedContentState("switch"), animatedContentScope),
            onClick = onSwitchToLogin
        ) {
            Text("Already have an account? Login", modifier = Modifier.skipToLookaheadSize())
        }
    }
}