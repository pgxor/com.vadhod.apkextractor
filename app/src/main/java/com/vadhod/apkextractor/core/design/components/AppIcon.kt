package com.vadhod.apkextractor.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.vadhod.apkextractor.core.design.LocalAppGradients
import com.vadhod.apkextractor.core.model.AppEntry
import com.vadhod.apkextractor.data.packages.AppIconRequest

/** App launcher icon on a soft halo tile, loaded & cached via Coil from PackageManager (offline). */
@Composable
fun AppIcon(
    app: AppEntry,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    val shape = RoundedCornerShape(size * 0.28f)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(LocalAppGradients.current.iconHalo),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = AppIconRequest(app.packageName, app.lastUpdateTime),
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .padding(size * 0.14f)
                .clip(shape),
        )
    }
}

/** Subtle placeholder used while content loads. */
@Composable
fun IconPlaceholder(size: Dp = 48.dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.28f))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    )
}
