package com.vadhod.apkextractor.data.settings

import com.vadhod.apkextractor.core.model.SortOrder
import com.vadhod.apkextractor.core.model.ThemeMode

/**
 * Immutable snapshot of user preferences, exposed as a stream by [SettingsRepository] and consumed
 * as UI state (rules.md §C-13). All fields have sensible defaults so a fresh install is well-behaved.
 *
 * - [exportTreeUri] is the persisted SAF tree URI (stringified) chosen via `OPEN_DOCUMENT_TREE`.
 */
data class Settings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val bundleSplitsByDefault: Boolean = true,
    val sortOrder: SortOrder = SortOrder.NAME_ASC,
    val exportTreeUri: String? = null,
    /** False until the user finishes (or skips) the first-run onboarding; replayable from Settings. */
    val onboardingCompleted: Boolean = false,
)
