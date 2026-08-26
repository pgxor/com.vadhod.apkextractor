package com.vadhod.apkextractor.feature.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vadhod.apkextractor.R
import com.vadhod.apkextractor.core.design.components.LabeledRow

private const val CONTACT_EMAIL = "parjanyagala@gmail.com"

/**
 * Compact About modal: identity and version, what the app is, credits, licence, copyright and a
 * contact address. Opened by tapping the About card in Settings.
 *
 * The version is read from [android.content.pm.PackageManager] rather than `BuildConfig` so we do
 * not have to switch on the `buildConfig` build feature for two fields.
 */
@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val version = remember(context) {
        runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            "${info.versionName} (${info.longVersionCode})"
        }.getOrDefault("\u2014")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Image(
                painter = painterResource(R.mipmap.ic_launcher),
                contentDescription = null,
                modifier = Modifier.size(64.dp).clip(MaterialTheme.shapes.medium),
            )
        },
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "Vadhod APK Extractor",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    "Version $version",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        },
        text = {
            Column(
                // Scrolls so the modal stays usable at large font scales instead of clipping the
                // copyright line — the same mistake the Settings screen used to make.
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "A fully offline APK extractor. Browse the apps installed on this device and " +
                        "save their APKs to a folder you pick. Split apps are bundled into a single " +
                        "reinstallable .apks archive.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "No internet permission. No tracking, no ads, no root.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )

                HorizontalDivider()

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LabeledRow("Developer", "Parjanya Gala")
                    LabeledRow(
                        label = "Contact",
                        value = CONTACT_EMAIL,
                        modifier = Modifier.heightIn(min = 44.dp),
                        onClick = { context.composeMail(version) },
                    )
                    LabeledRow("Licence", "GPL-3.0-or-later")
                }

                HorizontalDivider()

                Text(
                    "Built with Kotlin, Jetpack Compose and Material 3. App icons are loaded " +
                        "offline with Coil.\n\nTypeface: Nunito, Copyright 2014 The Nunito Project " +
                        "Authors, licensed under the SIL Open Font License 1.1. The full licence " +
                        "text ships with this app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "Copyright (C) 2026 Parjanya Gala",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        },
    )
}

/**
 * Hands the address to whatever email app the user has. `ACTION_SENDTO` with a `mailto:` URI opens
 * a composer — nothing is sent, and no network call happens from this app, which holds no
 * INTERNET permission (rules.md §A). Failure is a toast, never a crash, matching the share flow.
 */
private fun Context.composeMail(version: String) {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$CONTACT_EMAIL")).apply {
        putExtra(Intent.EXTRA_SUBJECT, "Vadhod APK Extractor $version")
    }
    runCatching { startActivity(intent) }.onFailure {
        Toast.makeText(this, "No email app found. Reach me at $CONTACT_EMAIL", Toast.LENGTH_LONG).show()
    }
}
