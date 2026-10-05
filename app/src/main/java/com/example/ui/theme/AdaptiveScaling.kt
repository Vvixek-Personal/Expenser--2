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
    val cardCornerRadius: Dp = SleekRadius.lg,
    val buttonCornerRadius: Dp = SleekRadius.md,
    val defaultPadding: Dp = SleekSpacing.lg,
    val compactPadding: Dp = SleekSpacing.sm,
    val buttonMinHeight: Dp = SleekSizes.buttonSmall,
    val iconSize: Dp = SleekSizes.iconSmall
) {
    val cardShape: RoundedCornerShape
        get() = RoundedCornerShape(cardCornerRadius)
    val buttonShape: RoundedCornerShape
        get() = RoundedCornerShape(buttonCornerRadius)
}

val LocalAdaptiveDimens = staticCompositionLocalOf { AdaptiveDimens() }

@Composable
fun AdaptiveScalingProvider(content: @Composable () -> Unit) {
    val fontScale = LocalConfiguration.current.fontScale

    // Adaptive logic: when font is large, shrink paddings and slightly flatten corners to prevent overflow and visual imbalance
    val adaptiveDimens = when {
        fontScale >= 1.4f -> AdaptiveDimens(
            cardCornerRadius = SleekRadius.sm,
            buttonCornerRadius = SleekRadius.xs,
            defaultPadding = SleekSpacing.sm,
            compactPadding = SleekSpacing.xs,
            buttonMinHeight = 32.dp,
            iconSize = SleekSizes.iconMicro
        )
        fontScale >= 1.2f -> AdaptiveDimens(
            cardCornerRadius = SleekRadius.md,
            buttonCornerRadius = SleekRadius.sm,
            defaultPadding = SleekSpacing.md,
            compactPadding = 6.dp,
            buttonMinHeight = 34.dp,
            iconSize = SleekSizes.iconSmall
        )
        else -> AdaptiveDimens(
            cardCornerRadius = SleekRadius.lg,
            buttonCornerRadius = SleekRadius.md,
            defaultPadding = SleekSpacing.lg,
            compactPadding = SleekSpacing.sm,
            buttonMinHeight = SleekSizes.buttonSmall,
            iconSize = SleekSizes.iconSmall
        )
    }

    CompositionLocalProvider(
        LocalAdaptiveDimens provides adaptiveDimens
    ) {
        content()
    }
}
