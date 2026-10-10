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
            fontWeight = FontWeight.SemiBold,
            fontSize = 32.sp,
            lineHeight = 38.sp
        ),
        displayMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 32.sp,
            lineHeight = 38.sp
        ),
        displaySmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 32.sp,
            lineHeight = 38.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = 28.sp
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
            fontSize = 22.sp,
            lineHeight = 28.sp
        ),
        titleLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 22.sp
        ),
        titleMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 22.sp
        ),
        titleSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        bodySmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),
        labelLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 18.sp
        ),
        labelMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),
        labelSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    )
}

val Typography: Typography
    get() = createAppTypography(activeFontFamilyChoiceState)

/**
 * Standardized typography and sizing scale for buttons, icons, and text across tabs.
 * Disciplined to max 5 font sizes (12, 14, 16, 22, 32) and only 2 weights (Normal, SemiBold).
 */
object SleekSizes {
    // Icon sizing scale
    val iconMicro = 16.dp
    val iconSmall = 18.dp
    val iconMedium = 20.dp
    val iconLarge = 24.dp
    val iconHero = 32.dp

    // Button height scale (min 48dp for touch targets)
    val buttonSmall = 48.dp
    val buttonMedium = 48.dp
    val buttonLarge = 56.dp
    val buttonHeightSmall = 48.dp
    val buttonHeightMedium = 48.dp
    val buttonHeightLarge = 56.dp

    // Strictly 5-step typography scale
    val textCaption = 12.sp
    val textBody = 14.sp
    val textTitle = 16.sp
    val textHeading = 22.sp
    val textHero = 32.sp

    // Aliases mapped strictly to the 5 permitted sizes
    val textMicro = 12.sp
    val textBodySmall = 12.sp
    val textBodyMedium = 14.sp
    val textBodyLarge = 14.sp
    val textSubhead = 16.sp
    val textTitleLarge = 16.sp
    val textHeadline = 22.sp
    val textHeadlineLarge = 22.sp
    val textDisplay = 32.sp
    val textMega = 32.sp
}

/**
 * Standardized corner radius tokens: ONLY 3 permitted radii.
 * 8dp for chips/inputs, 12dp for cards, 20dp for sheets/dialogs.
 */
object SleekRadius {
    val chip = 8.dp
    val input = 8.dp
    val card = 12.dp
    val sheet = 20.dp
    val dialog = 20.dp

    // Aliases preserved for compilation compatibility
    val xs = 8.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 12.dp
    val xl = 20.dp
    val xxl = 20.dp
    val pill = 20.dp
}

/**
 * Standardized RoundedCornerShapes matching the 3-radii system (8dp, 12dp, 20dp).
 */
object SleekShapes {
    val chip = RoundedCornerShape(8.dp)
    val input = RoundedCornerShape(8.dp)
    val card = RoundedCornerShape(12.dp)
    val sheet = RoundedCornerShape(20.dp)
    val dialog = RoundedCornerShape(20.dp)

    // Aliases preserved for compilation compatibility
    val xs = RoundedCornerShape(8.dp)
    val sm = RoundedCornerShape(8.dp)
    val md = RoundedCornerShape(12.dp)
    val lg = RoundedCornerShape(12.dp)
    val xl = RoundedCornerShape(20.dp)
    val xxl = RoundedCornerShape(20.dp)
    val pill = RoundedCornerShape(20.dp)
}

/**
 * Standardized layout spacing scale based strictly on the 4/8/12/16/24 grid.
 */
object SleekSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp

    // Screen and Card standard padding
    val screenPadding = 16.dp
    val cardPadding = 16.dp

    // Compatibility aliases constrained to 4/8/12/16/24 grid
    val xxs = 4.dp
    val xxl = 24.dp
    val xxxl = 24.dp
}

