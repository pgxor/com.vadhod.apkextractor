package com.vadhod.apkextractor.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vadhod.apkextractor.core.design.components.GradientBackground
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val art: OnboardingArt,
    val icon: ImageVector,
    val title: String,
    val body: String,
)

private val onboardingPages = listOf(
    OnboardingPage(
        art = OnboardingArt.PRIVACY,
        icon = Icons.Rounded.Lock,
        title = "Private by design",
        body = "Vadhod extracts your installed apps 100% offline. No internet permission, no tracking, " +
            "no ads — nothing ever leaves your device.",
    ),
    OnboardingPage(
        art = OnboardingArt.BOX,
        icon = Icons.Rounded.Inventory2,
        title = "Extract & bundle",
        body = "Save any app's APK to a folder you choose. Split apps are bundled into one " +
            "reinstallable .apks archive automatically.",
    ),
    OnboardingPage(
        art = OnboardingArt.SHARE,
        icon = Icons.Rounded.Share,
        title = "Inspect & share",
        body = "Peek inside any APK — signing certificate, contents and details — then share the file " +
            "or export its icon in a tap.",
    ),
)

/**
 * First-run (and replayable) onboarding: a 3-page pager with an animated 3D model per page, page
 * indicator, and Skip / Next / Get started controls. [onFinish] is called on Skip or Get started and
 * should persist the completed flag (see [com.vadhod.apkextractor.data.settings.SettingsRepository]).
 */
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = remember { onboardingPages }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                AnimatedVisibility(visible = !isLastPage) {
                    TextButton(onClick = onFinish) {
                        Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Shared 3D stage: one persistent scene that swaps its model as the page changes, with a
            // gentle counter-parallax against the swipe.
            // The whole page (art + text) lives in the pager, so a swipe anywhere changes pages,
            // with a gentle parallax + fade on the illustration.
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) { index ->
                val page = pages[index]
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        OnboardingIllustration(
                            art = page.art,
                            icon = page.icon,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    val offset = (pagerState.currentPage - index +
                                        pagerState.currentPageOffsetFraction).coerceIn(-1f, 1f)
                                    translationX = size.width * offset * 0.12f
                                    alpha = 1f - kotlin.math.abs(offset) * 0.4f
                                },
                        )
                    }
                    Text(
                        text = page.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = page.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }

            Spacer(Modifier.height(24.dp))
            PageIndicator(current = pagerState.currentPage, count = pages.size)
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isLastPage) onFinish()
                    else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp),
            ) {
                Text(if (isLastPage) "Get started" else "Next", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Pill-style page indicator: the active dot stretches into a bar, in the brand accent. */
@Composable
private fun PageIndicator(current: Int, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(count) { index ->
            val active = index == current
            val width by animateDpAsState(
                targetValue = if (active) 28.dp else 8.dp,
                animationSpec = spring(dampingRatio = 0.6f),
                label = "dotWidth",
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(
                        if (active) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                    ),
            )
        }
    }
}
