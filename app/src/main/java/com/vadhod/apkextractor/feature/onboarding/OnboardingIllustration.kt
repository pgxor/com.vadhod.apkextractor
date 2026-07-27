package com.vadhod.apkextractor.feature.onboarding

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.vadhod.apkextractor.core.design.LocalAppGradients
import kotlin.math.cos
import kotlin.math.sin

/** Which hand-drawn animation an onboarding page shows. */
enum class OnboardingArt { PRIVACY, BOX, SHARE }

/**
 * Fully Compose-drawn onboarding art — no assets, no dependency, 100% offline. [OnboardingArt.BOX] is
 * an isometric 3D box whose lid hinges open while an APK card pops out; the others are animated pastel
 * orbs carrying the page [icon]. All colors come from the design system (rules.md §D).
 */
@Composable
fun OnboardingIllustration(art: OnboardingArt, icon: ImageVector, modifier: Modifier = Modifier) {
    when (art) {
        OnboardingArt.BOX -> AnimatedBox(modifier)
        else -> AnimatedOrb(icon = icon, modifier = modifier)
    }
}

// --- The star: an isometric box opening -------------------------------------------------------

@Composable
private fun AnimatedBox(modifier: Modifier) {
    val scheme = MaterialTheme.colorScheme
    val halo = LocalAppGradients.current.iconHalo
    val transition = rememberInfiniteTransition(label = "box")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing)),
        label = "cycle",
    )
    val open = boxOpen(t)
    val lift = boxLift(t)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .blur(60.dp)
                .clip(CircleShape)
                .background(halo),
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawOpeningBox(
                open = open,
                lift = lift,
                primary = scheme.primary,
                secondary = scheme.secondary,
                tertiary = scheme.tertiary,
                card = scheme.surface,
            )
        }
    }
}

/** Lid open factor 0..1: opens, holds, closes over one loop. */
private fun boxOpen(t: Float): Float {
    val raw = when {
        t < 0.30f -> t / 0.30f
        t < 0.80f -> 1f
        else -> 1f - (t - 0.80f) / 0.20f
    }
    return smoothStep(raw.coerceIn(0f, 1f))
}

/** Item rise factor 0..1, slightly lagged behind the lid. */
private fun boxLift(t: Float): Float {
    val raw = when {
        t < 0.15f -> 0f
        t < 0.42f -> (t - 0.15f) / 0.27f
        t < 0.75f -> 1f
        else -> 1f - (t - 0.75f) / 0.25f
    }
    return smoothStep(raw.coerceIn(0f, 1f))
}

private fun smoothStep(x: Float): Float = x * x * (3f - 2f * x)

private fun DrawScopeLerp(a: Float, b: Float, f: Float) = a + (b - a) * f

/**
 * Draws an isometric box (painter's order: floor → back walls → lid → rising card → front walls) so
 * the card reads as emerging from inside. The lid rotates about the back-top edge by up to ~109°.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawOpeningBox(
    open: Float,
    lift: Float,
    primary: Color,
    secondary: Color,
    tertiary: Color,
    card: Color,
) {
    val s = 0.62f
    val boxH = 0.72f
    val scale = size.minDimension * 0.30f
    val cx = size.width / 2f
    val cy = size.height / 2f + size.minDimension * 0.05f

    fun proj(x: Float, y: Float, z: Float): Offset {
        val sx = (x - y) * 0.866f
        val sy = (x + y) * 0.5f - z
        return Offset(cx + sx * scale, cy + sy * scale)
    }
    fun quad(a: Offset, b: Offset, c: Offset, d: Offset, color: Color) {
        val path = Path().apply {
            moveTo(a.x, a.y); lineTo(b.x, b.y); lineTo(c.x, c.y); lineTo(d.x, d.y); close()
        }
        drawPath(path, color)
    }

    // Footprint: back p0(-s,-s), right p1(s,-s), front p2(s,s), left p3(-s,s).
    val p0b = proj(-s, -s, 0f); val p1b = proj(s, -s, 0f); val p2b = proj(s, s, 0f); val p3b = proj(-s, s, 0f)
    val p0t = proj(-s, -s, boxH); val p1t = proj(s, -s, boxH); val p3t = proj(-s, s, boxH); val p2t = proj(s, s, boxH)

    val interior = lerp(secondary, Color.Black, 0.22f)
    val floor = lerp(secondary, Color.Black, 0.34f)
    val faceLeft = lerp(primary, Color.Black, 0.12f)
    val faceRight = lerp(primary, Color.White, 0.08f)
    val lidTop = tertiary
    val cardAccent = primary

    // 1) interior floor + 2) back walls (inner surfaces seen through the opening)
    quad(p0b, p1b, p2b, p3b, floor)
    quad(p0b, p1b, p1t, p0t, interior)
    quad(p0b, p3b, p3t, p0t, interior)

    // 3) lid — hinge along back-top edge p0t..p1t; front edge lifts by theta
    val theta = open * 1.90f
    val dy = 2f * s
    val frontY = -s + dy * cos(theta)
    val frontZ = boxH + dy * sin(theta)
    val lidFrontR = proj(s, frontY, frontZ)
    val lidFrontL = proj(-s, frontY, frontZ)
    quad(p0t, p1t, lidFrontR, lidFrontL, lidTop)

    // 4) rising APK card (drawn after lid so it sits in front of the leaned-back lid)
    if (lift > 0.02f) {
        val itemZ = DrawScopeLerp(0.12f, boxH + 0.60f, lift)
        val center = proj(0f, 0f, itemZ)
        val w = size.minDimension * 0.19f
        val h = size.minDimension * 0.25f
        val alpha = lift.coerceIn(0f, 1f)
        drawRoundRect(
            color = card.copy(alpha = alpha),
            topLeft = Offset(center.x - w / 2f, center.y - h / 2f),
            size = Size(w, h),
            cornerRadius = CornerRadius(w * 0.18f),
        )
        drawRoundRect(
            color = cardAccent.copy(alpha = alpha),
            topLeft = Offset(center.x - w / 2f, center.y + h * 0.10f),
            size = Size(w, h * 0.30f),
            cornerRadius = CornerRadius(w * 0.12f),
        )
        // sparkles trailing the card
        val sparkle = primary.copy(alpha = alpha * 0.8f)
        drawCircle(sparkle, size.minDimension * 0.013f, proj(0.42f, 0.34f, itemZ * 0.9f))
        drawCircle(sparkle, size.minDimension * 0.017f, proj(-0.44f, 0.18f, itemZ * 1.05f))
        drawCircle(sparkle, size.minDimension * 0.010f, proj(0.08f, -0.40f, itemZ * 1.12f))
    }

    // 5) outer front walls last → occlude the card's lower half so it looks like it's inside
    quad(p2b, p1b, p1t, p2t, faceRight)
    quad(p3b, p2b, p2t, p3t, faceLeft)
}

// --- Supporting art: animated pastel orb ------------------------------------------------------

@Composable
private fun AnimatedOrb(icon: ImageVector, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "orb")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(18_000, easing = LinearEasing)),
        label = "rotation",
    )
    val breathe by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Reverse),
        label = "breathe",
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(230.dp)
                .scale(breathe)
                .rotate(rotation)
                .blur(48.dp)
                .clip(CircleShape)
                .background(LocalAppGradients.current.iconHalo),
        )
        Box(
            modifier = Modifier
                .size(168.dp)
                .scale(breathe)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer,
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .size(72.dp)
                    .graphicsLayer { rotationY = rotation },
            )
        }
    }
}
