package com.github.huymaster.textguardian.android.ui.activity

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.huymaster.textguardian.android.ui.theme.KMessengerTheme

class ChatActivity : BaseActivity() {
    @Composable
    override fun Content(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        val navController = rememberNavController()
        Navigation(navController)
    }

    @Composable
    private fun Navigation(controller: NavHostController) {
        BackHandler { controller.popBackStack() }
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            NavHost(navController = controller, startDestination = "chat") {

            }
        }
    }

    @Preview
    @Composable
    private fun Preview() {
        KMessengerTheme {
            Content(null, null)
        }
    }
}