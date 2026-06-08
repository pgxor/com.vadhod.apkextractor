package com.vadhod.apkextractor.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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
        content()
    }
}
