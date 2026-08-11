/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.di

import kpt.core.database.AppDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin module that provides the [AppDatabase] instance and the framework-infra DAO singletons.
 *
 * Delegates platform-specific database construction to [platformModule], which each
 * source set (`androidMain`, `desktopMain`, `nativeMain`, `jsMain`, `wasmJsMain`)
 * implements using the appropriate [AppDatabaseFactory][kpt.core.base.database.AppDatabaseFactory]
 * and SQLite driver.
 *
 * INFRA-ONLY, owner: template (E1 / C3). The demo DAO providers + the ChargeTypeConverters install
 * relocated to the fork-owned [kpt.core.database.demo.di.DemoDatabaseModule]; this aggregator carries
 * ZERO `kpt.core.*.demo.*` imports so a template sync can blind-copy it without re-introducing demo
 * wiring a fork already stripped.
 */
val DatabaseModule = module {
    includes(platformModule)
    // infra (framework) — always kept
    single { get<AppDatabase>().bookkeeperDao }
}

/**
 * Platform-specific Koin module that provides the [AppDatabase] singleton.
 *
 * Each platform actual configures the database builder with the correct
 * [SQLiteDriver][androidx.sqlite.SQLiteDriver] and [CoroutineDispatcher][kotlinx.coroutines.CoroutineDispatcher]:
 * - **Android/Desktop**: [BundledSQLiteDriver][androidx.sqlite.driver.bundled.BundledSQLiteDriver] + `Dispatchers.IO`
 * - **Native (iOS)**: [BundledSQLiteDriver][androidx.sqlite.driver.bundled.BundledSQLiteDriver] + `Dispatchers.Default`
 * - **JS/WasmJS**: SQLiteWeb driver (OPFS-backed) + `Dispatchers.Default`
 */
expect val platformModule: Module
