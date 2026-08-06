/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.addtowatchlist.di

import kpt.feature.addtowatchlist.ui.AddToWatchlistViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for the embedded add-to-watchlist star (`submit_offline_write` demo).
 * [AddToWatchlistViewModel] takes a runtime `coinId` parameter (`parametersOf(coinId)`),
 * so it is registered as a parameterised `viewModel { params -> ... }`. The
 * [kpt.core.data.demo.watchlist.WatchlistRepository] is DI-provided and shared with feat-watchlist.
 */
val AddToWatchlistModule = module {
    viewModel { params ->
        AddToWatchlistViewModel(repository = get(), coinId = params.get())
    }
}
