package com.vadhod.apkextractor.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppEntryAndSortTest {

    private fun entry(
        pkg: String,
        label: String,
        size: Long = 0,
        installed: Long = 0,
        updated: Long = 0,
        splits: List<String> = emptyList(),
    ) = AppEntry(
        packageName = pkg,
        label = label,
        versionName = "1.0",
        versionCode = 1,
        isSystem = false,
        baseApkPath = "/data/app/$pkg/base.apk",
        splitApkPaths = splits,
        totalSizeBytes = size,
        firstInstallTime = installed,
        lastUpdateTime = updated,
        minSdk = 29,
        targetSdk = 37,
    )

    @Test
    fun isSplit_reflects_split_paths() {
        assertFalse(entry("a", "A").isSplit)
        assertTrue(entry("b", "B", splits = listOf("/x/split.apk")).isSplit)
    }

    @Test
    fun allApkPaths_puts_base_first() {
        val e = entry("b", "B", splits = listOf("/x/s1.apk", "/x/s2.apk"))
        assertEquals(listOf("/data/app/b/base.apk", "/x/s1.apk", "/x/s2.apk"), e.allApkPaths)
    }

    @Test
    fun sort_by_name_is_case_insensitive() {
        val list = listOf(entry("a", "banana"), entry("b", "Apple"), entry("c", "cherry"))
        assertEquals(
            listOf("Apple", "banana", "cherry"),
            list.sortedByOrder(SortOrder.NAME_ASC).map { it.label },
        )
    }

    @Test
    fun sort_by_size_desc() {
        val list = listOf(entry("a", "A", size = 10), entry("b", "B", size = 50), entry("c", "C", size = 30))
        assertEquals(listOf(50L, 30L, 10L), list.sortedByOrder(SortOrder.SIZE_DESC).map { it.totalSizeBytes })
    }

    @Test
    fun sort_by_updated_newest() {
        val list = listOf(entry("a", "A", updated = 100), entry("b", "B", updated = 300), entry("c", "C", updated = 200))
        assertEquals(listOf("B", "C", "A"), list.sortedByOrder(SortOrder.UPDATED_NEWEST).map { it.label })
    }
}
