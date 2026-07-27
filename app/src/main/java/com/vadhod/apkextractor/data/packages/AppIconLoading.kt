package com.vadhod.apkextractor.data.packages

import android.content.pm.PackageManager
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.key.Keyer
import coil3.request.Options

/**
 * Coil model identifying an installed app's launcher icon. [version] (the app's `lastUpdateTime`) is
 * part of the identity so the cached icon refreshes when the app is updated.
 */
data class AppIconRequest(val packageName: String, val version: Long)

/** Stable cache key for [AppIconRequest] so icons are memoised across recompositions/scroll. */
class AppIconKeyer : Keyer<AppIconRequest> {
    override fun key(data: AppIconRequest, options: Options): String =
        "app-icon:${data.packageName}:${data.version}"
}

/**
 * Loads an app icon straight from [PackageManager] — fully offline, no network, no bundled assets
 * (architecture.md §7.1). Wired into Coil's singleton loader in `App.newImageLoader`.
 */
class AppIconFetcher(
    private val packageManager: PackageManager,
    private val request: AppIconRequest,
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        val drawable = packageManager.getApplicationIcon(request.packageName)
        return ImageFetchResult(
            image = drawable.asImage(),
            isSampled = false,
            dataSource = DataSource.DISK,
        )
    }

    class Factory(private val packageManager: PackageManager) : Fetcher.Factory<AppIconRequest> {
        override fun create(
            data: AppIconRequest,
            options: Options,
            imageLoader: ImageLoader,
        ): Fetcher = AppIconFetcher(packageManager, data)
    }
}
