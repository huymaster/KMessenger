package com.github.huymaster.textguardian.android.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.github.huymaster.textguardian.android.app.AppSettingsManager

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@ExperimentalMaterial3ExpressiveApi
@Composable
fun KMessengerTheme(
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val theme by AppSettingsManager.Settings.THEME
    val context = LocalContext.current
    val darkTheme = when (theme) {
        AppSettingsManager.Settings.THEME.DARK -> true
        AppSettingsManager.Settings.THEME.LIGHT -> false
        else -> isSystemInDarkTheme()
    }
    val colorScheme = when (darkTheme) {
        true -> dynamicDarkColorScheme(context)
        false -> dynamicLightColorScheme(context)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        content = content,
        typography = Typography
    )
}