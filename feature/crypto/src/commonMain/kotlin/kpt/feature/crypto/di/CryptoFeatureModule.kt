/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.crypto.di

import kpt.feature.crypto.ui.CoinMarketsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for the crypto feature.
 *
 * Wired into the app graph by `cmp-navigation/.../KoinModules.kt`
 * (`featureModule.includes(CryptoFeatureModule)`). The `CryptoRepository`
 * binding is provided by `core/data/.../RepositoryModule.kt`; the
 * `ScreenUiStateStore` binding lives in `core-base/datastore/.../DatastoreBaseModule.kt`
 * (surfaced as a singleton) and is transitively wired via
 * `DatastoreModule` in the parent graph.
 */
val CryptoFeatureModule = module {
    viewModel { params ->
        CoinMarketsViewModel(
            routeKey = params.get(),
            repository = get(),
            screenUiStateStore = get(),
        )
    }
}
