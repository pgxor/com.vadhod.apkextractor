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
 * App theme. Fixed Anthropic/Claude pastel palette — **no** Material You dynamic color
 * (questionnaire Q5). Light + "dim pastel" dark (Q4). Also publishes [LocalAppGradients].
 */
@Composable
fun VadhodTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
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
