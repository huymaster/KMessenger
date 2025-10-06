package com.github.huymaster.textguardian.android.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.huymaster.textguardian.android.data.repository.AppSettingsManager
import com.github.huymaster.textguardian.android.ui.model.ServerHealthModel
import com.github.huymaster.textguardian.android.ui.state.MainUiState
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.get

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
class MainActivity : BaseActivity() {
    private val serverHealthModel: ServerHealthModel by viewModel()
    private val settings = get<AppSettingsManager>()

    @Composable
    override fun Content(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        Scaffold { contentPadding ->
            Surface(modifier = Modifier.padding(contentPadding)) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ServerHealth()
                }
            }
        }
    }

    @Composable
    private fun ServerHealth() {
        val state by serverHealthModel.serverHealthState.collectAsState()
        LaunchedEffect(Unit) {
            serverHealthModel.getServerHealth()
        }
        LaunchedEffect(state) {
            if (state is MainUiState.Success) {
                if (serverHealthModel.isSessionValid)
                    navigateToChatList()
                else
                    navigateToAuthentication()
            }
        }
        Crossfade(
            targetState = state
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (it) {
                    is MainUiState.Loading -> {
                        LoadingIndicator()
                        Text("Loading...")
                    }

                    is MainUiState.ValidatingSession -> {
                        LoadingIndicator()
                        Text("Validating session...")
                    }

                    is MainUiState.Error -> {
                        Text(it.message)
                        TextButton(onClick = { serverHealthModel.getServerHealth() }) {
                            Text("Retry")
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    private fun navigateToAuthentication() {
        val intent = Intent(this, AuthenticationActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun navigateToChatList() {
        val intent = Intent(this, `ChatActivity`::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}