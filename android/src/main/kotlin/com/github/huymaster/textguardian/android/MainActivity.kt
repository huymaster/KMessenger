package com.github.huymaster.textguardian.android

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.github.huymaster.textguardian.android.activity.BaseFragmentActivity
import com.github.huymaster.textguardian.android.ui.theme.KMessengerTheme
import kotlinx.coroutines.delay

class MainActivity : BaseFragmentActivity() {
    lateinit var prompt: BiometricPrompt
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle("TextGuardian")
        .setSubtitle("Biometric authentication")
        .setNegativeButtonText("Cancel")
        .build()
    var isAuthenticated = false


    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    override fun Content(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        // Long loading after 5 seconds
        var longLoading by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            delay(5000)
            longLoading = true
        }
        Scaffold { contentPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LoadingIndicator()
                        AnimatedContent(
                            modifier = Modifier.fillMaxWidth(),
                            targetState = longLoading,
                            transitionSpec = {
                                (fadeIn() + slideInVertically { it } togetherWith
                                        fadeOut() + slideOutVertically { -it }).using(SizeTransform(clip = false))
                            }
                        ) {
                            if (it)
                                Text(
                                    "Loading... (this take long time than expected)",
                                    textAlign = TextAlign.Center
                                )
                            else
                                Text(
                                    "Loading...",
                                    textAlign = TextAlign.Center
                                )
                        }
                    }
                }
            }
        }
    }

    @Preview
    @Composable
    fun Preview() {
        KMessengerTheme {
            Content(null,null)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(this@MainActivity, errString, Toast.LENGTH_SHORT).show()
                    finishAndRemoveTask()
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        if (!isAuthenticated)
            prompt.authenticate(info)
    }
}