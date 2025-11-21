@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)

package com.github.huymaster.textguardian.android.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.huymaster.textguardian.android.app.ApplicationEvents
import com.github.huymaster.textguardian.android.app.JWTTokenManager
import com.github.huymaster.textguardian.android.data.repository.CipherRepository
import com.github.huymaster.textguardian.android.ui.screen.ChatListScreen
import com.github.huymaster.textguardian.android.ui.screen.ChatScreen
import com.github.huymaster.textguardian.android.ui.screen.NewChatScreen
import com.github.huymaster.textguardian.android.ui.utils.with
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.get

class ChatActivity : BaseActivity() {
    private val applicationEvents = get<ApplicationEvents>()

    private enum class Screen {
        CHAT_LIST, NEW_CHAT, CHAT
    }

    @Composable
    override fun Content(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        Surface { ActivityContent() }
    }

    @Composable
    private fun ActivityContent() {
        val navController = rememberNavController()
        val sessionExpired by get<ApplicationEvents>().sessionExpired.collectAsState(false)

        LaunchedEffect(sessionExpired) { if (sessionExpired) moveToMainActivity() }

        SharedTransitionLayout {
            NavHost(
                navController = navController, startDestination = Screen.CHAT_LIST.name
            ) {
                composable(
                    route = Screen.CHAT_LIST.name,
                ) {
                    ChatListScreen(
                        transitionBundle = this@SharedTransitionLayout with this,
                        navController = navController
                    )
                }
                composable(
                    route = Screen.NEW_CHAT.name,
                    enterTransition = { slideInVertically(animationSpec = tween(300)) { it } + fadeIn() },
                    popExitTransition = { slideOutVertically(animationSpec = tween(300)) { it } + fadeOut() }
                ) {
                    NewChatScreen(
                        transitionBundle = this@SharedTransitionLayout with this,
                        navController = navController
                    )
                }
                composable(
                    route = "${Screen.CHAT.name}/{conversationId}",
                    enterTransition = { slideInHorizontally(animationSpec = tween(300)) { it } },
                    popExitTransition = { slideOutHorizontally(animationSpec = tween(300)) { it } }
                ) {
                    val conversationId = navController.currentBackStackEntry?.arguments?.getString("conversationId")
                    ChatScreen(
                        transitionBundle = this@SharedTransitionLayout with this,
                        navController = navController,
                        conversationId = conversationId
                    )
                }
            }
        }
    }

    override fun onCreateEx(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        get<JWTTokenManager>().autoCheckToken(lifecycleScope)
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.IO).launch { get<CipherRepository>().sendPublicKey() }
    }

    private fun moveToMainActivity() {
        AlertDialog.Builder(this).setTitle("Session Expired")
            .setMessage("Your session has expired. Please login again.").setPositiveButton("OK") { _, _ ->
                val mainIntent = Intent(this, MainActivity::class.java)
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(mainIntent)
                finish()
            }.show()
    }
}