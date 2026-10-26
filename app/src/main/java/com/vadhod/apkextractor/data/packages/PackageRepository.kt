package com.vadhod.apkextractor.data.packages

import com.vadhod.apkextractor.core.model.AppEntry

/**
 * Single source of truth for the installed-app list (rules.md §C-14). Splits the catalogue into the
 * two tabs the UI shows (System vs User) per architecture.md §7.1.
 */
class PackageRepository(private val source: PackageManagerSource) {

    data class Partitioned(
        val user: List<AppEntry>,
        val system: List<AppEntry>,
    )

    /** Change sequence the current list reflects; read *before* loading so a change mid-load still counts. */
    @Volatile private var loadedSequence = 0

    /** True when an app was installed, updated or removed since the last [getPartitioned]. */
    suspend fun hasChangedSinceLastLoad(): Boolean = source.changeSequence(loadedSequence) != loadedSequence

    suspend fun getPartitioned(): Partitioned {
        loadedSequence = source.changeSequence(loadedSequence)
        val all = source.loadAll()
        return Partitioned(
            user = all.filterNot { it.isSystem },
            system = all.filter { it.isSystem },
        )
    }
}
