package com.github.huymaster.textguardian.android.ui.component

import androidx.compose.animation.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.github.huymaster.textguardian.android.app.AppSettingsManager

@Composable
fun QuickThemeButton(
    modifier: Modifier = Modifier
) {
    var theme by remember { AppSettingsManager.Settings.THEME }
    var mode by remember { mutableIntStateOf(AppSettingsManager.Settings.THEME.VALUES.indexOf(theme)) }
    LaunchedEffect(mode) {
        theme = AppSettingsManager.Settings.THEME.VALUES[mode]
    }

    AnimatedContent(
        targetState = theme,
        transitionSpec = {
            (fadeIn() + slideInVertically { it } togetherWith fadeOut() + slideOutVertically { it })
                .using(SizeTransform(clip = true))
        }
    ) {
        val icon = when (it) {
            AppSettingsManager.Settings.THEME.DARK -> Icons.Default.DarkMode
            AppSettingsManager.Settings.THEME.LIGHT -> Icons.Default.LightMode
            else -> Icons.Default.BrightnessAuto
        }
        IconButton(
            modifier = modifier,
            onClick = { mode = (mode + 1) % AppSettingsManager.Settings.THEME.VALUES.size }
        ) {
            Icon(icon, contentDescription = "Theme")
        }
    }
}