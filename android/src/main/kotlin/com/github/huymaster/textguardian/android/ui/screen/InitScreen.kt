package com.github.huymaster.textguardian.android.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.huymaster.textguardian.android.viewmodel.InitViewModel

@Composable
fun InitScreen(
    onServiceAvailable: (Boolean) -> Unit
) {
    val model = viewModel<InitViewModel>()
    val state by model.state.collectAsStateWithLifecycle()
    var message by remember { mutableStateOf("") }
    var retry by remember { mutableLongStateOf(0L) }
    LaunchedEffect(retry) { model.checkServiceHealth() }
    LaunchedEffect(state) {
        message = "Loading..."
        if (state.isServiceAvailable) {
            message = "Validating session..."
            onServiceAvailable(model.validateSession())
        }
    }
    InitScreenContent(
        isLoading = state.isLoading,
        isServiceAvailable = state.isServiceAvailable,
        message = message,
        error = state.error,
        retry = { retry = System.currentTimeMillis() }
    )
}

@Composable
private fun InitScreenContent(
    isLoading: Boolean,
    isServiceAvailable: Boolean,
    message: String? = null,
    error: String?,
    retry: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                    Text(message ?: "", modifier = Modifier.padding(16.dp))
                } else if (!isServiceAvailable) {
                    Text(error ?: "Service is not available", modifier = Modifier.padding(16.dp))
                    TextButton(onClick = retry) { Text("Retry") }
                }
            }
        }
    }
}