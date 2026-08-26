package com.vadhod.apkextractor.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.vadhod.apkextractor.core.design.LocalAppGradients

/** Full-screen soft pastel gradient wash used behind every screen (architecture.md §8). */
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LocalAppGradients.current.background),
    ) {
        // Every screen sits in a Scaffold with containerColor = Transparent so this gradient shows
        // through. Material3 then resolves the Scaffold content colour via
        // contentColorFor(Transparent), which matches nothing in the ColorScheme and falls back to
        // the LocalContentColor default — a hard-coded Color.Black. Any Text without an explicit
        // colour therefore painted pure black, unreadable on the dark gradient. A Box publishes no
        // content colour of its own, so anchor it here once, for every screen.
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            content()
        }
    }
}
