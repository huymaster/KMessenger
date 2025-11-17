package com.github.huymaster.textguardian.android.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.github.huymaster.textguardian.android.R

val ubuntuLight = Font(R.font.ubuntu_light, weight = FontWeight.Light)
val ubuntuLightItalic = Font(R.font.ubuntu_light_italic, weight = FontWeight.Light, style = FontStyle.Italic)
val ubuntuRegular = Font(R.font.ubuntu_regular, weight = FontWeight.Normal)
val ubuntuItalic = Font(R.font.ubuntu_italic, weight = FontWeight.Normal, style = FontStyle.Italic)
val ubuntuMedium = Font(R.font.ubuntu_medium, weight = FontWeight.Medium)
val ubuntuMediumItalic = Font(R.font.ubuntu_medium_italic, weight = FontWeight.Medium, style = FontStyle.Italic)
val ubuntuBold = Font(R.font.ubuntu_bold, weight = FontWeight.Bold)
val ubuntuBoldItalic = Font(R.font.ubuntu_bold_italic, weight = FontWeight.Bold, style = FontStyle.Italic)
val ubuntuMonoRegular = Font(R.font.ubuntu_mono_regular, weight = FontWeight.Normal)
val ubuntuMonoItalic = Font(R.font.ubuntu_mono_italic, weight = FontWeight.Normal, style = FontStyle.Italic)
val ubuntuMonoBold = Font(R.font.ubuntu_mono_bold, weight = FontWeight.Bold)
val ubuntuMonoBoldItalic = Font(R.font.ubuntu_mono_bold_italic, weight = FontWeight.Bold, style = FontStyle.Italic)

val ubuntuFonts = FontFamily(
    ubuntuLight,
    ubuntuLightItalic,
    ubuntuRegular,
    ubuntuItalic,
    ubuntuMedium,
    ubuntuMediumItalic,
    ubuntuBold,
    ubuntuBoldItalic,
)

val DefaultTypography = Typography()
val UbuntuTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = ubuntuFonts,
        fontWeight = FontWeight(400),
        fontSize = 57.sp,
        lineHeight = 64.sp
    ),
    displayMedium = TextStyle(
        fontFamily = ubuntuFonts,
        fontWeight = FontWeight(400),
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontFamily = ubuntuFonts,
        fontWeight = FontWeight(400),
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = ubuntuFonts,
        fontWeight = FontWeight(400),
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = ubuntuFonts,
        fontWeight = FontWeight(400),
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = ubuntuFonts,
        fontWeight = FontWeight(400),
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    titleLarge = TextStyle(
        fontFamily = ubuntuFonts,
        fontWeight = FontWeight(400),
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = ubuntuFonts,
        fontWeight = FontWeight(500),
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = ubuntuFonts,
        fontWeight = FontWeight(500),
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = ubuntuFonts,
        fontWeight = FontWeight(500),
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = ubuntuFonts,
        fontWeight = FontWeight(500),
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = ubuntuFonts,
        fontWeight = FontWeight(500),
        fontSize = 11.sp,
        lineHeight = 16.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = ubuntuFonts,
        fontWeight = FontWeight(400),
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = ubuntuFonts,
        fontWeight = FontWeight(400),
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = ubuntuFonts,
        fontWeight = FontWeight(400),
        fontSize = 24.sp,
        lineHeight = 32.sp
    )
)
