package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Typography
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

var activeFontFamilyChoiceState by mutableStateOf("Default")

fun getFontFamilyForChoice(choice: String): FontFamily {
    return when (choice) {
        "Sans Serif" -> FontFamily.SansSerif
        "Rounded / Casual" -> FontFamily.Default
        "Tech Monospace" -> FontFamily.Monospace
        "Classic Serif" -> FontFamily.Serif
        else -> FontFamily.SansSerif
    }
}

val modernFontFamily: FontFamily
    get() = getFontFamilyForChoice(activeFontFamilyChoiceState)

fun createAppTypography(choice: String): Typography {
    val family = getFontFamilyForChoice(choice)
    return Typography(
        displayLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = 42.sp,
            lineHeight = 48.sp,
            letterSpacing = (-1.0).sp
        ),
        displayMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            lineHeight = 40.sp,
            letterSpacing = (-0.5).sp
        ),
        displaySmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            letterSpacing = (-0.25).sp
        ),
        headlineLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            lineHeight = 32.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = 28.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 26.sp
        ),
        titleLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = 24.sp
        ),
        titleMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            lineHeight = 20.sp
        ),
        titleSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 18.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 22.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 18.sp
        ),
        bodySmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            lineHeight = 16.sp
        ),
        labelLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.2.sp
        ),
        labelMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.4.sp
        ),
        labelSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            letterSpacing = 0.5.sp
        )
    )
}

val Typography: Typography
    get() = createAppTypography(activeFontFamilyChoiceState)

/**
 * Standardized typography and sizing scale for buttons, icons, and text across tabs.
 */
object SleekSizes {
    // Icon sizing scale
    val iconMicro = 14.dp
    val iconSmall = 18.dp
    val iconMedium = 22.dp
    val iconLarge = 28.dp
    val iconHero = 36.dp

    // Button height scale
    val buttonSmall = 36.dp
    val buttonMedium = 48.dp
    val buttonLarge = 56.dp

    // Typography sp scale
    val textMicro = 10.sp
    val textCaption = 11.sp
    val textBodySmall = 12.sp
    val textBodyMedium = 13.sp
    val textBody = 14.sp
    val textBodyLarge = 15.sp
    val textSubhead = 16.sp
    val textTitle = 18.sp
    val textTitleLarge = 20.sp
    val textHeadline = 22.sp
    val textHeadlineLarge = 24.sp
    val textDisplay = 28.sp
    val textHero = 34.sp
    val textMega = 42.sp
}

/**
 * Standardized corner radius tokens to replace ad-hoc radius values across all components.
 */
object SleekRadius {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val pill = 999.dp
}

/**
 * Predefined RoundedCornerShapes matching [SleekRadius] for cards, surfaces, and buttons.
 */
object SleekShapes {
    val xs = RoundedCornerShape(4.dp)
    val sm = RoundedCornerShape(8.dp)
    val md = RoundedCornerShape(12.dp)
    val lg = RoundedCornerShape(16.dp)
    val xl = RoundedCornerShape(20.dp)
    val xxl = RoundedCornerShape(24.dp)
    val pill = RoundedCornerShape(999.dp)
}

/**
 * Standardized layout spacing scale based on 4dp/8dp grid.
 */
object SleekSpacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
}

