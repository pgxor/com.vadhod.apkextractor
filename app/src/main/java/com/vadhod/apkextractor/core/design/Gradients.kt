package com.vadhod.apkextractor.core.design

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.vadhod.apkextractor.core.design.color.Pastel

/**
 * Very low-contrast pastel gradients (architecture.md §8). Defined once here and consumed via
 * [LocalAppGradients]; never hard-coded per screen (rules.md §D-19).
 */
@Immutable
data class AppGradients(
    val background: Brush,
    val primary: Brush,
    val cardAccent: Brush,
    val iconHalo: Brush,
)

private fun softLinear(colors: List<Color>) = Brush.linearGradient(colors)

val LightGradients = AppGradients(
    background = softLinear(listOf(Color(0xFFFCFAF6), Color(0xFFF3EFE6), Color(0xFFF6EEE9))),
    primary = softLinear(listOf(Color(0xFFE2906E), Pastel.Clay, Pastel.ClayDeep)),
    cardAccent = softLinear(listOf(Pastel.Manilla, Pastel.SageSoft)),
    iconHalo = softLinear(listOf(Color(0xFFF7E9DF), Color(0xFFEDE7DA))),
)

val DarkGradients = AppGradients(
    background = softLinear(listOf(Color(0xFF1C1B18), Color(0xFF201E1B), Color(0xFF24221E))),
    primary = softLinear(listOf(Color(0xFFE9A082), Color(0xFFD98C6B), Color(0xFFCC785C))),
    cardAccent = softLinear(listOf(Color(0xFF332F28), Color(0xFF2C322D))),
    iconHalo = softLinear(listOf(Color(0xFF2A2823), Color(0xFF262421))),
)

val LocalAppGradients = staticCompositionLocalOf { LightGradients }
