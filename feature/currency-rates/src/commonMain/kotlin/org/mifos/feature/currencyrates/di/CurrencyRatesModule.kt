/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.currencyrates.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.mifos.core.store.AppStoreRegistry
import org.mifos.feature.currencyrates.ui.CurrencyRatesViewModel
import org.mifos.feature.currencyrates.ui.RateHistoryViewModel

val CurrencyRatesModule = module {
    viewModel {
        CurrencyRatesViewModel(
            currencyRepository = get(),
            networkMonitor = get(),
            fetchedAtRepository = get(),
            spotRateStore = get(qualifier = AppStoreRegistry.SpotRate),
        )
    }
    viewModel { RateHistoryViewModel(get()) }
}
