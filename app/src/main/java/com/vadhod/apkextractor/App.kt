package com.vadhod.apkextractor

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.memory.MemoryCache
import com.vadhod.apkextractor.data.extract.ApkExtractor
import com.vadhod.apkextractor.data.inspect.ApkInspector
import com.vadhod.apkextractor.data.packages.AppIconFetcher
import com.vadhod.apkextractor.data.packages.AppIconKeyer
import com.vadhod.apkextractor.data.packages.PackageManagerSource
import com.vadhod.apkextractor.data.packages.PackageRepository
import com.vadhod.apkextractor.data.settings.SettingsRepository
import com.vadhod.apkextractor.core.util.DefaultDispatcherProvider

/**
 * Application entry point. Builds the manual DI [AppContainer] and configures Coil's singleton
 * [ImageLoader] with the [AppIconFetcher] so app icons load (and cache) offline.
 *
 * No DI framework on purpose — the graph is tiny and explicit (keeps the dependency surface small,
 * libraries-used.md §4).
 */
class App : Application(), SingletonImageLoader.Factory {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                add(AppIconKeyer())
                add(AppIconFetcher.Factory(applicationContext.packageManager))
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .build()
}

/** Tiny manual dependency container, held by [App] and reached from ViewModels. */
class AppContainer(app: App) {
    private val dispatchers = DefaultDispatcherProvider
    private val packageManager = app.packageManager

    val packageRepository = PackageRepository(PackageManagerSource(packageManager, dispatchers))
    val apkExtractor = ApkExtractor(dispatchers)
    val apkInspector = ApkInspector(packageManager, dispatchers)
    val settingsRepository = SettingsRepository(app)
}
