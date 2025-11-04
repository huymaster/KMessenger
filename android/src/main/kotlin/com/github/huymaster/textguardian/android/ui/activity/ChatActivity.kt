@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.github.huymaster.textguardian.android.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.github.huymaster.textguardian.android.app.ApplicationEvents
import com.github.huymaster.textguardian.android.app.JWTTokenManager
import com.github.huymaster.textguardian.android.ui.screen.ChatListScreen
import org.koin.core.component.get

class ChatActivity : BaseActivity() {
    private val applicationEvents = get<ApplicationEvents>()

    private enum class Screen {
        CHAT_LIST,
        NEW_CHAT,
        CHAT
    }

    @Composable
    override fun Content(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        val navController = rememberNavController()
        val sessionExpired by get<ApplicationEvents>().sessionExpired.collectAsState(false)
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        var showFloatingActionButton by remember { mutableStateOf(false) }

        LaunchedEffect(sessionExpired) {
            if (sessionExpired) moveToMainActivity()
        }
        LaunchedEffect(navBackStackEntry) {
            showFloatingActionButton = navBackStackEntry?.destination?.route == Screen.CHAT_LIST.name
        }

        Scaffold(
            floatingActionButton = {
                if (showFloatingActionButton)
                    FloatingActionButton(
                        onClick = {
                            navController.navigate(Screen.NEW_CHAT.name) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "New")
                    }
            }
        ) { paddingValues ->
            Surface(modifier = Modifier.padding(paddingValues)) {
                NavHost(navController = navController, startDestination = Screen.CHAT_LIST.name) {
                    composable(Screen.CHAT_LIST.name) {
                        ChatListScreen()
                    }
                    composable(Screen.NEW_CHAT.name) {
                    }
                    composable("${Screen.CHAT.name}/{id}") {
                    }
                }
            }
        }
    }

    override fun onCreateEx(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        get<JWTTokenManager>().autoCheckToken(lifecycleScope)
    }

    private fun moveToMainActivity() {
        AlertDialog.Builder(this)
            .setTitle("Session Expired")
            .setMessage("Your session has expired. Please login again.")
            .setPositiveButton("OK") { _, _ ->
                val mainIntent = Intent(this, MainActivity::class.java)
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(mainIntent)
                finish()
            }
            .show()
    }
}