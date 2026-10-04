package com.example.ui

import android.provider.Settings
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/** True only when the in-app switch is ON and Android's animator scale is not 0. */
val LocalAnimationsEnabled = compositionLocalOf { true }

@Composable
fun systemAnimationsAllowed(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
}

fun <T> appSpring(
    enabled: Boolean,
    dampingRatio: Float = Spring.DampingRatioNoBouncy,
    stiffness: Float = Spring.StiffnessMedium
): FiniteAnimationSpec<T> =
    if (enabled) spring(dampingRatio, stiffness) else snap()

fun <T> appTween(
    enabled: Boolean,
    durationMs: Int,
    easing: Easing = FastOutSlowInEasing
): FiniteAnimationSpec<T> =
    if (enabled) tween(durationMs, easing = easing) else snap()

@Composable
fun <T> appTween(durationMs: Int, easing: Easing = FastOutSlowInEasing): FiniteAnimationSpec<T> =
    if (LocalAnimationsEnabled.current) tween(durationMs, easing = easing) else snap()

@Composable
fun <T> appSpring(
    dampingRatio: Float = Spring.DampingRatioNoBouncy,
    stiffness: Float = Spring.StiffnessMedium
): FiniteAnimationSpec<T> =
    if (LocalAnimationsEnabled.current) spring(dampingRatio, stiffness) else snap()

/** Looping float that holds [rest] (no animation, no frame work) when animations are off. */
@Composable
fun rememberLoopFloat(
    initial: Float,
    target: Float,
    durationMs: Int,
    label: String,
    easing: Easing = LinearEasing,
    reverse: Boolean = false,
    rest: Float = initial
): Float {
    if (!LocalAnimationsEnabled.current) return rest
    val transition = rememberInfiniteTransition(label = label)
    val value by transition.animateFloat(
        initialValue = initial,
        targetValue = target,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = easing),
            repeatMode = if (reverse) RepeatMode.Reverse else RepeatMode.Restart
        ),
        label = label
    )
    return value
}

/**
 * Delightful spring scale feedback on interactive touch press.
 * Compresses slightly when tapped and springs back on release.
 * Honors [LocalAnimationsEnabled] — stays 1.0f when animations are disabled.
 */
fun Modifier.bouncyClickable(
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = null,
    enabled: Boolean = true,
    scaleDown: Float = 0.95f,
    onClick: () -> Unit
): Modifier = composed {
    val animationsEnabled = LocalAnimationsEnabled.current
    val actualSource = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by actualSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && animationsEnabled) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bouncy_click_scale"
    )
    this
        .scale(scale)
        .clickable(
            interactionSource = actualSource,
            indication = indication,
            enabled = enabled,
            onClick = onClick
        )
}

/**
 * Spring press scale modifier for Cards and custom surfaces.
 */
fun Modifier.bouncyPress(scaleDown: Float = 0.96f): Modifier = composed {
    val animationsEnabled = LocalAnimationsEnabled.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && animationsEnabled) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bouncy_press_scale"
    )
    this.scale(scale)
}

/**
 * Smooth numeric count-up animation for balances, stats, and scores.
 */
@Composable
fun animateAmountFloat(
    targetValue: Float,
    durationMs: Int = 750
): Float {
    val animationsEnabled = LocalAnimationsEnabled.current
    if (!animationsEnabled) return targetValue
    val animVal by animateFloatAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = durationMs, easing = FastOutSlowInEasing),
        label = "amount_count_up"
    )
    return animVal
}

/**
 * Dynamic pulsing shimmer gradient brush for cards and banners.
 */
@Composable
fun rememberAnimatedGlowBrush(
    baseColor: Color,
    accentColor: Color,
    durationMs: Int = 2600
): Brush {
    val offset = rememberLoopFloat(
        initial = -200f,
        target = 800f,
        durationMs = durationMs,
        label = "glow_brush_offset",
        reverse = false,
        rest = 0f
    )
    return Brush.linearGradient(
        colors = listOf(baseColor, accentColor, baseColor),
        start = Offset(offset - 250f, offset - 250f),
        end = Offset(offset + 250f, offset + 250f)
    )
}

/**
     * Staggered entrance modifier with slide-up and fade-in physics.
     */
@Composable
fun rememberStaggeredProgress(delayMs: Int = 0, durationMs: Int = 450): Float {
    val animationsEnabled = LocalAnimationsEnabled.current
    if (!animationsEnabled) return 1f
    var visible by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (delayMs > 0) kotlinx.coroutines.delay(delayMs.toLong())
        visible = true
    }
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = durationMs, easing = FastOutSlowInEasing),
        label = "staggered_entrance"
    )
    return progress
}

