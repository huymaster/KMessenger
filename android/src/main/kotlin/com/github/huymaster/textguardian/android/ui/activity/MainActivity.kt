@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)

package com.github.huymaster.textguardian.android.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.compose.animation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.huymaster.textguardian.android.ui.component.QuickThemeButton
import com.github.huymaster.textguardian.android.ui.screen.InitScreen
import com.github.huymaster.textguardian.android.ui.screen.LoginScreen
import com.github.huymaster.textguardian.android.ui.screen.RegisterScreen

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
                        0 -> InitScreen { haveSession ->
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
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                QuickThemeButton(
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                )
                SharedTransitionLayout {
                    AnimatedContent(screen, transitionSpec = { fadeIn() togetherWith fadeOut() }) {
                        when (it) {
                            AuthenticationScreen.LOGIN.ordinal -> LoginScreen(
                                this@SharedTransitionLayout,
                                this,
                                { screen = AuthenticationScreen.REGISTER.ordinal }
                            ) { moveToChatActivity() }

                            AuthenticationScreen.REGISTER.ordinal -> RegisterScreen(
                                this@SharedTransitionLayout,
                                this,
                                onSwitchToLogin = { screen = AuthenticationScreen.LOGIN.ordinal }
                            )
                        }
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