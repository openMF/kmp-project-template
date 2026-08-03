/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.watchlist.di

import kpt.feature.watchlist.ui.WatchlistViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for the Watchlist feature (`read_local_list` demo).
 * [WatchlistViewModel] takes no parameters — it reads reactively from the
 * DI-provided [kpt.core.data.demo.watchlist.WatchlistRepository].
 */
val WatchlistModule = module {
    viewModel { WatchlistViewModel(repository = get()) }
}
