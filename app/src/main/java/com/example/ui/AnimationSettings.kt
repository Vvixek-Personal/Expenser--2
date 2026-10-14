package com.example.ui

import android.provider.Settings
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
 * Adds high-fidelity tactile depth, elevation drop-shadow, and bouncy spring press physics to Buttons and interactive components.
 */
fun Modifier.tactileDepth(
    elevation: Dp = 3.dp,
    shape: Shape = RoundedCornerShape(12.dp),
    scaleDown: Float = 0.96f
): Modifier = composed {
    val animationsEnabled = LocalAnimationsEnabled.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedElevation by animateDpAsState(
        targetValue = if (isPressed && animationsEnabled) 1.dp else elevation,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tactile_elevation"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed && animationsEnabled) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tactile_scale"
    )
    this
        .scale(scale)
        .shadow(elevation = animatedElevation, shape = shape, clip = false)
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

/**
 * Subtle organic breathing pulse animation modifier for hero banners, active badges, and focus elements.
 */
fun Modifier.subtleBreathing(
    minScale: Float = 0.985f,
    maxScale: Float = 1.015f,
    durationMs: Int = 3200
): Modifier = composed {
    val scale = rememberLoopFloat(
        initial = minScale,
        target = maxScale,
        durationMs = durationMs,
        label = "subtle_breathing_scale",
        easing = FastOutSlowInEasing,
        reverse = true,
        rest = 1f
    )
    this.scale(scale)
}

/**
 * Lightweight hardware-accelerated celebration particle canvas for milestone, budget success, and savings celebrations.
 */
@Composable
fun CelebrationParticleBurst(
    trigger: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 24
) {
    if (!trigger || !LocalAnimationsEnabled.current) return

    val animProgress = remember { androidx.compose.animation.core.Animatable(0f) }

    androidx.compose.runtime.LaunchedEffect(trigger) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing)
        )
    }

    val colors = remember {
        listOf(
            Color(0xFF10B981), // Emerald
            Color(0xFF3B82F6), // Blue
            Color(0xFFF59E0B), // Amber
            Color(0xFF8B5CF6), // Purple
            Color(0xFFEC4899), // Pink
            Color(0xFF06B6D4)  // Cyan
        )
    }

    val particles = remember {
        val random = java.util.Random(42)
        List(particleCount) { index ->
            val angle = (index.toFloat() / particleCount.toFloat()) * 2f * Math.PI.toFloat() + (random.nextFloat() * 0.4f - 0.2f)
            val distance = 60f + random.nextFloat() * 120f
            val size = 5f + random.nextFloat() * 7f
            val color = colors[index % colors.size]
            Triple(angle, distance, Pair(size, color))
        }
    }

    if (animProgress.value < 1f) {
        androidx.compose.foundation.Canvas(modifier = modifier.fillMaxSize()) {
            val progress = animProgress.value
            val alpha = (1f - progress).coerceIn(0f, 1f)
            val center = Offset(size.width / 2f, size.height / 2f)

            particles.forEach { (angle, maxDist, sizeAndColor) ->
                val currentDist = maxDist * (1f - (1f - progress) * (1f - progress))
                val px = center.x + kotlin.math.cos(angle) * currentDist
                val py = center.y + kotlin.math.sin(angle) * currentDist + (progress * progress * 40f) // gentle gravity
                val pSize = sizeAndColor.first * (1f - progress * 0.4f)
                drawCircle(
                    color = sizeAndColor.second.copy(alpha = alpha),
                    radius = pSize,
                    center = Offset(px, py)
                )
            }
        }
    }
}
