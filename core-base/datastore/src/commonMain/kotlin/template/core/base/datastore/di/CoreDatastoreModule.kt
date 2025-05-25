/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.datastore.di

import com.russhwolf.settings.Settings
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.mifos.core.common.di.AppDispatchers
import template.core.base.datastore.contracts.ReactiveDataStore
import template.core.base.datastore.factory.DataStoreFactory
import template.core.base.datastore.reactive.PreferenceFlowOperators
import template.core.base.datastore.repository.ReactivePreferencesRepository

/**
 * Koin module for providing core datastore dependencies.
 *
 * Usage Example:
 * ```kotlin
 * startKoin {
 *     modules(CoreDatastoreModule)
 * }
 * ```
 */
val CoreDatastoreModule = module {

    // Platform-specific Settings instance
    single<Settings> { Settings() }

    // Main reactive datastore repository (recommended for most use cases)
    single<ReactivePreferencesRepository> {
        DataStoreFactory()
            .settings(get())
            .dispatcher(get(named(AppDispatchers.IO.name)))
            .cacheSize(200)
            .build()
    }

    // Direct access to reactive datastore (if needed for specific use cases)
    single<ReactiveDataStore> {
        DataStoreFactory()
            .settings(get())
            .dispatcher(get(named(AppDispatchers.IO.name)))
            .cacheSize(200)
            .buildDataStore()
    }

    // Flow operators for advanced reactive operations
    single<PreferenceFlowOperators> {
        PreferenceFlowOperators(get<ReactivePreferencesRepository>())
    }
}
