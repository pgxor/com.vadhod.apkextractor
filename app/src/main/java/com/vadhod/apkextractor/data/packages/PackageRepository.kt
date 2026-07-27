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

    suspend fun getPartitioned(): Partitioned {
        val all = source.loadAll()
        return Partitioned(
            user = all.filterNot { it.isSystem },
            system = all.filter { it.isSystem },
        )
    }
}
