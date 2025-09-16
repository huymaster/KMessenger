package com.github.huymaster.textguardian.android

import android.os.Bundle
import android.os.PersistableBundle
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddModerator
import androidx.compose.material.icons.filled.GifBox
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.huymaster.textguardian.android.activity.BaseActivity
import com.github.huymaster.textguardian.android.ui.theme.KMessengerTheme
import kotlin.random.Random

class MainActivity : BaseActivity() {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    override fun Content(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        var expanded by remember { mutableStateOf(false) }
        var progress by remember { mutableFloatStateOf(0f) }
        var inderterminate by remember { mutableStateOf(false) }
        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessVeryLow,
                    visibilityThreshold = 1 / 1000f,
                ),
        )
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("K Messenger") }
                )
            },
            floatingActionButton = {
                FloatingActionButtonMenu(
                    expanded = expanded,
                    button = {
                        ToggleFloatingActionButton(
                            modifier = Modifier
                                .semantics {
                                    traversalIndex = -1f
                                    stateDescription = if (expanded) "Expanded" else "Collapsed"
                                    contentDescription = "Toggle menu"
                                },
                            checked = expanded,
                            onCheckedChange = { expanded = it }
                        ) {
                            Icon(Icons.Default.Menu, null)
                        }
                    }
                ) {
                    repeat(10) {
                        FloatingActionButtonMenuItem(
                            onClick = {},
                            text = { Text("Item $it") },
                            icon = { Icon(Icons.Default.AddModerator, null) }
                        )
                    }
                }
            }
        ) { contentPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(128.dp)
                            .wrapContentSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Crossfade(inderterminate) {
                            if (it)
                                LoadingIndicator()
                            else
                                LoadingIndicator(progress = { animatedProgress })
                        }
                    }
                    Spacer(Modifier.requiredHeight(30.dp))
                    Text("Set loading progress:")
                    Slider(
                        modifier = Modifier.width(300.dp),
                        value = progress,
                        enabled = !inderterminate,
                        valueRange = 0f..1f,
                        onValueChange = { progress = it },
                    )
                    Checkbox(inderterminate, { inderterminate = it })
                }
            }
        }
    }

    @Preview(showBackground = true, showSystemUi = true)
    @Composable
    fun PreviewContent() {
        KMessengerTheme {
            Content(null, null)
        }
    }
}