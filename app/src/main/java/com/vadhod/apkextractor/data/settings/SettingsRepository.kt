package com.vadhod.apkextractor.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vadhod.apkextractor.core.model.SortOrder
import com.vadhod.apkextractor.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Single source of truth for user preferences, backed by DataStore Preferences (architecture.md §4).
 * Enums are stored by [Enum.name] and decoded defensively so an unknown/renamed value falls back to
 * the default rather than throwing.
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val themeMode = stringPreferencesKey("theme_mode")
        val bundleSplits = booleanPreferencesKey("bundle_splits_by_default")
        val sortOrder = stringPreferencesKey("sort_order")
        val exportTreeUri = stringPreferencesKey("export_tree_uri")
        val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
    }

    val settings: Flow<Settings> = context.dataStore.data.map { prefs ->
        Settings(
            themeMode = prefs[Keys.themeMode].toEnumOr(ThemeMode.SYSTEM) { ThemeMode.valueOf(it) },
            bundleSplitsByDefault = prefs[Keys.bundleSplits] ?: true,
            sortOrder = prefs[Keys.sortOrder].toEnumOr(SortOrder.NAME_ASC) { SortOrder.valueOf(it) },
            exportTreeUri = prefs[Keys.exportTreeUri],
            onboardingCompleted = prefs[Keys.onboardingCompleted] ?: false,
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.themeMode] = mode.name }
    }

    suspend fun setBundleSplitsByDefault(value: Boolean) {
        context.dataStore.edit { it[Keys.bundleSplits] = value }
    }

    suspend fun setSortOrder(order: SortOrder) {
        context.dataStore.edit { it[Keys.sortOrder] = order.name }
    }

    suspend fun setExportTreeUri(uri: String) {
        context.dataStore.edit { it[Keys.exportTreeUri] = uri }
    }

    suspend fun setOnboardingCompleted(value: Boolean) {
        context.dataStore.edit { it[Keys.onboardingCompleted] = value }
    }

    private inline fun <T> String?.toEnumOr(default: T, parse: (String) -> T): T =
        this?.let { runCatching { parse(it) }.getOrNull() } ?: default
}
