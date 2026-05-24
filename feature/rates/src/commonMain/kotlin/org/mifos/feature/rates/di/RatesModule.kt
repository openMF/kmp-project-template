/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.rates.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.mifos.feature.rates.ui.InterestRateDetailViewModel
import org.mifos.feature.rates.ui.InterestRatesViewModel

/**
 * Koin module for the B7 Interest Rate Tracker.
 *
 * Wire into the host app's `KoinModules` by adding `RatesModule` alongside the
 * other feature modules (`HomeModule`, `CurrencyRatesModule`, etc.) and adding
 * `include(":feature:rates")` to `settings.gradle.kts`. Both are deliberately
 * out-of-scope for this sub-plan — wiring lives in shared integration files.
 */
val RatesModule = module {
    // List screen — single-arg constructor that delegates to the default repository-backed factory.
    viewModel { InterestRatesViewModel(repository = get()) }

    // Detail screen — `seriesId` comes from the nav route via koinViewModel { parametersOf(seriesId) }.
    viewModel { params ->
        InterestRateDetailViewModel(
            seriesId = params.get(),
            repository = get(),
        )
    }
}
