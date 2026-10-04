package com.example.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AdaptiveDimens(
    val cardCornerRadius: Dp = 16.dp,
    val buttonCornerRadius: Dp = 12.dp,
    val defaultPadding: Dp = 16.dp,
    val compactPadding: Dp = 8.dp
)

val LocalAdaptiveDimens = staticCompositionLocalOf { AdaptiveDimens() }

@Composable
fun AdaptiveScalingProvider(content: @Composable () -> Unit) {
    val fontScale = LocalConfiguration.current.fontScale

    // Adaptive logic: when font is large, shrink paddings and slightly flatten corners to prevent overflow and visual imbalance
    val adaptiveDimens = when {
        fontScale >= 1.4f -> AdaptiveDimens(
            cardCornerRadius = 8.dp,
            buttonCornerRadius = 6.dp,
            defaultPadding = 8.dp,
            compactPadding = 4.dp
        )
        fontScale >= 1.2f -> AdaptiveDimens(
            cardCornerRadius = 12.dp,
            buttonCornerRadius = 8.dp,
            defaultPadding = 12.dp,
            compactPadding = 6.dp
        )
        else -> AdaptiveDimens(
            cardCornerRadius = 16.dp,
            buttonCornerRadius = 12.dp,
            defaultPadding = 16.dp,
            compactPadding = 8.dp
        )
    }

    CompositionLocalProvider(
        LocalAdaptiveDimens provides adaptiveDimens
    ) {
        content()
    }
}
