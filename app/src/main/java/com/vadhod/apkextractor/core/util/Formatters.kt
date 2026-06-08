package com.vadhod.apkextractor.core.util

import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ln
import kotlin.math.pow

/** Human-readable byte size, e.g. 1536 -> "1.5 KB", 0 -> "0 B". */
fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (ln(bytes.toDouble()) / ln(1024.0)).toInt().coerceIn(0, units.lastIndex)
    val value = bytes / 1024.0.pow(digitGroups.toDouble())
    return if (digitGroups == 0) "$bytes B"
    else String.format(Locale.getDefault(), "%.1f %s", value, units[digitGroups])
}

/** Locale-aware medium date, e.g. "Jun 8, 2026". */
fun formatDate(epochMillis: Long): String =
    if (epochMillis <= 0) "—"
    else DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(epochMillis))
