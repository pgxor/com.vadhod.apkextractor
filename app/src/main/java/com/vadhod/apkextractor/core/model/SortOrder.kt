package com.vadhod.apkextractor.core.model

/** User-selectable orderings for the app lists. Default is [NAME_ASC] (questionnaire Q11). */
enum class SortOrder {
    NAME_ASC,
    NAME_DESC,
    SIZE_DESC,
    SIZE_ASC,
    INSTALL_NEWEST,
    UPDATED_NEWEST,
}

/** Returns a new list ordered by [order]. Name comparisons are case-insensitive. */
fun List<AppEntry>.sortedByOrder(order: SortOrder): List<AppEntry> = when (order) {
    SortOrder.NAME_ASC -> sortedBy { it.label.lowercase() }
    SortOrder.NAME_DESC -> sortedByDescending { it.label.lowercase() }
    SortOrder.SIZE_DESC -> sortedByDescending { it.totalSizeBytes }
    SortOrder.SIZE_ASC -> sortedBy { it.totalSizeBytes }
    SortOrder.INSTALL_NEWEST -> sortedByDescending { it.firstInstallTime }
    SortOrder.UPDATED_NEWEST -> sortedByDescending { it.lastUpdateTime }
}
