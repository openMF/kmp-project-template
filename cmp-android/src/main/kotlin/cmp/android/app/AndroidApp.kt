/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.android.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import cmp.shared.generated.WorkerKmpAuto
import cmp.shared.utils.initKoin
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.request.CachePolicy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kpt.core.base.ui.util.getDefaultImageLoader
import kpt.core.data.user.UserDataRepository
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android application class.
 *
 * Worker-kmp single-API setup (Shape 2 — bring-your-own-Application from the wiki guide):
 * after `initKoin { androidContext(this@AndroidApp); ... }`, call `WorkerKmpAuto.install()`.
 * The auto-generated `installWorkerKmpAndroid()` reads the Application from Koin's
 * `androidContext()` binding, selects `androidWorkManagerFactory`, builds the worker
 * registry from `@WorkerKmpWorkers` annotation sites, and calls `WorkerKmpHost.initialize`.
 *
 * Migrated from v3.1.x's hand-rolled `loadKoinModules(workKoinModule(WorkerConfig(),
 * workerRegistry { register<…> { … } }, androidWorkManagerFactory(this)))` block plus the
 * `Sync.initialize(get<WorkScheduler>())` call — both eliminated in v4.0.0 in favor of the
 * single `WorkerKmpAuto.install()` line.
 */
class AndroidApp : Application(), SingletonImageLoader.Factory, KoinComponent {

    private val userDataRepository: UserDataRepository by inject()

    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@AndroidApp)
            androidLogger()
        }

        // Single line — codegen handles factory selection + worker registry + Koin wiring
        // + WorkerKmpHost.initialize. See @WorkerKmpWorkers annotation in cmp-shared's
        // WorkerDeclarations.kt for the declared workers.
        WorkerKmpAuto.install()

        // Restore the user's saved language preference to AppCompatDelegate.
        restoreSavedLanguage()
    }

    /**
     * Restores the user's saved language preference from the repository to AppCompatDelegate.
     *
     * This runs BEFORE any Activities are created, ensuring the app launches with the
     * correct language. The app's saved preference always takes precedence.
     */
    private fun restoreSavedLanguage() {
        runBlocking {
            val userData = userDataRepository.userData.first()
            val savedLanguage = userData.appLanguage

            val desiredLocales = if (savedLanguage.localeName != null) {
                LocaleListCompat.forLanguageTags(savedLanguage.localeName)
            } else {
                LocaleListCompat.getEmptyLocaleList()
            }

            val currentLocales = AppCompatDelegate.getApplicationLocales()
            if (currentLocales != desiredLocales) {
                AppCompatDelegate.setApplicationLocales(desiredLocales)
            }
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader = getDefaultImageLoader(context)
        .newBuilder()
        .diskCachePolicy(CachePolicy.ENABLED)
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("image_cache"))
                .maxSizePercent(0.25)
                .build()
        }
        .build()
}
