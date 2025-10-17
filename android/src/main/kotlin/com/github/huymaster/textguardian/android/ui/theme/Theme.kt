package com.github.huymaster.textguardian.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
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
    val colorScheme = when {
        dynamicColor -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        content = content,
        typography = Typography
    )
}