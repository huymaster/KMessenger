package com.github.huymaster.textguardian.android.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.compose.animation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.huymaster.textguardian.android.ui.screen.InitScreen
import com.github.huymaster.textguardian.android.ui.screen.LoginScreen
import com.github.huymaster.textguardian.android.ui.screen.RegisterScreen
import com.github.huymaster.textguardian.android.viewmodel.LoginViewModel
import com.github.huymaster.textguardian.android.viewmodel.RegisterViewModel

@ExperimentalMaterial3ExpressiveApi
@ExperimentalSharedTransitionApi
class MainActivity : BaseActivity() {
    @Composable
    override fun Content(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        var screen by rememberSaveable { mutableIntStateOf(0) }
        LaunchedEffect(screen) {
            if (screen == 2) moveToChatActivity()
        }
        Scaffold { paddingValues ->
            Surface(modifier = Modifier.padding(paddingValues)) {
                Crossfade(screen) {
                    when (it) {
                        0 -> InitScreen(viewModel()) { haveSession ->
                            screen = if (!haveSession) 1 else 2
                        }

                        1 -> AuthenticationScreen()
                    }
                }
            }
        }
    }

    private enum class AuthenticationScreen(val tabName: String) {
        LOGIN("Login"), REGISTER("Register")
    }

    @Composable
    private fun AuthenticationScreen() {
        var screen by rememberSaveable { mutableIntStateOf(AuthenticationScreen.LOGIN.ordinal) }
        val loginModel: LoginViewModel = viewModel()
        val registerModel: RegisterViewModel = viewModel()
        LaunchedEffect(screen) {
            loginModel.resetState()
            registerModel.resetState()
        }
        Column(
            modifier = Modifier
                .scrollable(rememberScrollState(), orientation = Orientation.Vertical)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SecondaryTabRow(
                modifier = Modifier.fillMaxWidth(),
                selectedTabIndex = screen,
            ) {
                AuthenticationScreen.entries.forEachIndexed { index, tab ->
                    Tab(selected = screen == index, onClick = { screen = index }, text = { Text(tab.tabName) })
                }
            }
            SharedTransitionLayout {
                AnimatedContent(screen, transitionSpec = { fadeIn() togetherWith fadeOut() }) {
                    when (it) {
                        AuthenticationScreen.LOGIN.ordinal -> LoginScreen(
                            loginModel,
                            this@SharedTransitionLayout,
                            this,
                            { screen = AuthenticationScreen.REGISTER.ordinal }
                        ) { moveToChatActivity() }

                        AuthenticationScreen.REGISTER.ordinal -> RegisterScreen(
                            registerModel,
                            this@SharedTransitionLayout,
                            this,
                            onSwitchToLogin = { screen = AuthenticationScreen.LOGIN.ordinal }
                        )
                    }
                }
            }
        }
    }

    private fun moveToChatActivity() {
        finish()
        startActivity(Intent(this, ChatActivity::class.java))
    }
}