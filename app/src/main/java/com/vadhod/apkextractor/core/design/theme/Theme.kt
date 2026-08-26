package com.vadhod.apkextractor.core.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.vadhod.apkextractor.core.design.DarkGradients
import com.vadhod.apkextractor.core.design.LightGradients
import com.vadhod.apkextractor.core.design.LocalAppGradients
import com.vadhod.apkextractor.core.design.color.DarkPastelColors
import com.vadhod.apkextractor.core.design.color.LightPastelColors
import com.vadhod.apkextractor.core.model.ThemeMode

/**
 * Resolves a [ThemeMode] to "is the UI currently dark". Shared with [VadhodTheme] so callers that
 * live outside the Compose colour scheme — notably the system-bar appearance in `MainActivity` —
 * stay in sync with the in-app theme override instead of the system night setting.
 */
@Composable
fun isDarkTheme(themeMode: ThemeMode): Boolean = when (themeMode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

/**
 * App theme. Fixed Anthropic/Claude pastel palette — **no** Material You dynamic color
 * (questionnaire Q5). Light + "dim pastel" dark (Q4). Also publishes [LocalAppGradients].
 */
@Composable
fun VadhodTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = isDarkTheme(themeMode)
    val colors = if (dark) DarkPastelColors else LightPastelColors
    val gradients = if (dark) DarkGradients else LightGradients

    CompositionLocalProvider(LocalAppGradients provides gradients) {
        MaterialTheme(
            colorScheme = colors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
