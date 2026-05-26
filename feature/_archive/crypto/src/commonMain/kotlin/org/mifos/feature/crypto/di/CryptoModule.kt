/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.crypto.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.mifos.feature.crypto.ui.CoinDetailViewModel
import org.mifos.feature.crypto.ui.CryptoWatchlistViewModel

val CryptoModule = module {
    viewModel { CryptoWatchlistViewModel(get()) }
    viewModel { params -> CoinDetailViewModel(get(), params.get()) }
}
