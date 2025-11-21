@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.github.huymaster.textguardian.android.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.huymaster.textguardian.android.ui.theme.KMessengerTheme
import kotlin.system.exitProcess

class ErrorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val threadName = intent.getStringExtra("thread") ?: "unknown"
        val errorString = intent.getStringExtra("exception") ?: "Unknown error"

        setContent {
            KMessengerTheme {
                Scaffold {
                    ExceptionView(
                        Modifier.padding(it), threadName, errorString
                    )
                }
            }
        }
    }

    @Composable
    private fun ExceptionView(modifier: Modifier, thread: String, exception: String) {
        var zoom by remember { mutableFloatStateOf(1f) }
        val azoom by animateFloatAsState(zoom)

        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                modifier = Modifier.padding(12.dp),
                text = "Application crashed [$thread]",
                style = MaterialTheme.typography.titleLarge
            )
            SelectionContainer(
                Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, _, nz, _ ->
                                zoom = (zoom * nz).coerceIn(1f, 4f)
                            }
                        }
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = exception,
                        fontFamily = FontFamily.Monospace,
                        fontSize = (9 * azoom).sp,
                        color = MaterialTheme.colorScheme.error,
                        lineHeight = (10 * azoom).sp
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                TextButton(
                    { exitProcess(1) }
                ) { Text("Exit") }
                Button(
                    { restart() }
                ) { Text("Restart") }
            }
        }
    }

    private fun restart() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}