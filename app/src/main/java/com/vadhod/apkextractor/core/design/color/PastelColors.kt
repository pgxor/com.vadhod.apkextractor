package com.vadhod.apkextractor.core.design.color

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * The Anthropic / Claude-inspired soft pastel palette (architecture.md §8, questionnaire Q2/Q3).
 * Warm ivory paper, clay/coral accent, kraft tan, manilla, soft sage, warm charcoal ink.
 * Contrast for body text is tuned for WCAG AA (rules.md §D-21).
 */
object Pastel {
    // Warm neutrals (light)
    val Ivory = Color(0xFFFAF9F5)
    val Paper = Color(0xFFF0EEE6)
    val PaperDim = Color(0xFFE9E6DC)
    val CardWhite = Color(0xFFFFFFFF)
    val Outline = Color(0xFFD9D6CB)
    val OutlineSoft = Color(0xFFE7E4DA)

    // Brand accent — clay / coral
    val Clay = Color(0xFFD97757)
    val ClayDeep = Color(0xFFCC785C) // book cloth
    val ClaySoft = Color(0xFFF6E1D7)
    val ClayInk = Color(0xFF5A2A17)

    // Secondary — kraft tan
    val Kraft = Color(0xFFD4A27F)
    val KraftSoft = Color(0xFFF3E6D7)
    val KraftInk = Color(0xFF4A3422)

    // Tertiary — sage
    val Sage = Color(0xFF7FA08C)
    val SageSoft = Color(0xFFDCE7DF)
    val SageInk = Color(0xFF26382E)

    // Manilla (warm cream accent)
    val Manilla = Color(0xFFEBDBBC)

    // Ink / text (warm charcoal, never pure black)
    val Ink = Color(0xFF1F1E1D)
    val InkSoft = Color(0xFF3D3D3A)
    val Muted = Color(0xFF6B6A64)

    // Dark ("dim pastel" — warm charcoal)
    val DarkBg = Color(0xFF1A1916)
    val DarkSurface = Color(0xFF1F1E1D)
    val DarkSurfaceHigh = Color(0xFF2A2825)
    val DarkSurfaceHigher = Color(0xFF302F2C)
    val DarkOutline = Color(0xFF49463F)
    val DarkOnSurface = Color(0xFFF0EEE6)
    val DarkMuted = Color(0xFFBDB9AE)
    val ClayLight = Color(0xFFE9A082)
    val KraftLight = Color(0xFFE2BD98)
    val SageLight = Color(0xFFA8C4B4)

    val Error = Color(0xFFB3261E)
    val ErrorSoft = Color(0xFFF9DEDC)
}

val LightPastelColors = lightColorScheme(
    primary = Pastel.Clay,
    onPrimary = Color.White,
    primaryContainer = Pastel.ClaySoft,
    onPrimaryContainer = Pastel.ClayInk,
    secondary = Pastel.Kraft,
    onSecondary = Color.White,
    secondaryContainer = Pastel.KraftSoft,
    onSecondaryContainer = Pastel.KraftInk,
    tertiary = Pastel.Sage,
    onTertiary = Color.White,
    tertiaryContainer = Pastel.SageSoft,
    onTertiaryContainer = Pastel.SageInk,
    background = Pastel.Ivory,
    onBackground = Pastel.Ink,
    surface = Pastel.Ivory,
    onSurface = Pastel.InkSoft,
    surfaceVariant = Pastel.Paper,
    onSurfaceVariant = Pastel.Muted,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFBFAF6),
    surfaceContainer = Pastel.Paper,
    surfaceContainerHigh = Color(0xFFECE9DF),
    surfaceContainerHighest = Pastel.PaperDim,
    outline = Pastel.Outline,
    outlineVariant = Pastel.OutlineSoft,
    error = Pastel.Error,
    onError = Color.White,
    errorContainer = Pastel.ErrorSoft,
    onErrorContainer = Color(0xFF410E0B),
)

val DarkPastelColors = darkColorScheme(
    primary = Pastel.ClayLight,
    onPrimary = Color(0xFF3A1A0C),
    primaryContainer = Color(0xFF6A3A28),
    onPrimaryContainer = Color(0xFFFFD9C7),
    secondary = Pastel.KraftLight,
    onSecondary = Color(0xFF3A2A18),
    secondaryContainer = Color(0xFF5A4533),
    onSecondaryContainer = Color(0xFFF3E4D5),
    tertiary = Pastel.SageLight,
    onTertiary = Color(0xFF1F3329),
    tertiaryContainer = Color(0xFF3A5247),
    onTertiaryContainer = Color(0xFFDCE7DF),
    background = Pastel.DarkBg,
    onBackground = Pastel.DarkOnSurface,
    surface = Pastel.DarkSurface,
    onSurface = Pastel.DarkOnSurface,
    surfaceVariant = Pastel.DarkSurfaceHigh,
    onSurfaceVariant = Pastel.DarkMuted,
    surfaceContainerLowest = Color(0xFF141310),
    surfaceContainerLow = Pastel.DarkSurface,
    surfaceContainer = Pastel.DarkSurfaceHigh,
    surfaceContainerHigh = Pastel.DarkSurfaceHigher,
    surfaceContainerHighest = Color(0xFF3A3833),
    outline = Pastel.DarkOutline,
    outlineVariant = Color(0xFF38362F),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
)
