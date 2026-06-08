package com.vadhod.apkextractor.core.design.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.vadhod.apkextractor.R

/** Nunito — rounded, friendly (questionnaire Q6). Bundled variable font; weights via FontVariation. */
@OptIn(ExperimentalTextApi::class)
private fun nunito(weight: Int) = Font(
    resId = R.font.nunito,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val Nunito = FontFamily(
    nunito(400),
    nunito(500),
    nunito(600),
    nunito(700),
    nunito(800),
)

/** Material 3 type scale with Nunito applied to every style. */
val AppTypography: Typography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = Nunito, fontWeight = FontWeight.Bold),
        displayMedium = displayMedium.copy(fontFamily = Nunito, fontWeight = FontWeight.Bold),
        displaySmall = displaySmall.copy(fontFamily = Nunito, fontWeight = FontWeight.Bold),
        headlineLarge = headlineLarge.copy(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold),
        headlineMedium = headlineMedium.copy(fontFamily = Nunito, fontWeight = FontWeight.Bold),
        headlineSmall = headlineSmall.copy(fontFamily = Nunito, fontWeight = FontWeight.Bold),
        titleLarge = titleLarge.copy(fontFamily = Nunito, fontWeight = FontWeight.Bold),
        titleMedium = titleMedium.copy(fontFamily = Nunito, fontWeight = FontWeight.SemiBold),
        titleSmall = titleSmall.copy(fontFamily = Nunito, fontWeight = FontWeight.SemiBold),
        bodyLarge = bodyLarge.copy(fontFamily = Nunito),
        bodyMedium = bodyMedium.copy(fontFamily = Nunito),
        bodySmall = bodySmall.copy(fontFamily = Nunito),
        labelLarge = labelLarge.copy(fontFamily = Nunito, fontWeight = FontWeight.SemiBold),
        labelMedium = labelMedium.copy(fontFamily = Nunito, fontWeight = FontWeight.Medium),
        labelSmall = labelSmall.copy(fontFamily = Nunito, fontWeight = FontWeight.Medium),
    )
}
