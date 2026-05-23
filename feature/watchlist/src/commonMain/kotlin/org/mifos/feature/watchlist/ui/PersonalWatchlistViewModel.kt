/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.watchlist.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.mifos.core.data.watchlist.WatchlistItem
import org.mifos.core.data.watchlist.WatchlistRepository
import template.core.base.store.screen.DataFreshness
import template.core.base.store.screen.ScreenState
import template.core.base.ui.viewmodel.BaseViewModel

/**
 * Read-side ViewModel for [PersonalWatchlistScreen].
 *
 * Maps the [WatchlistRepository.watchlist] flow into [ScreenState] so the screen renders
 * via `ScreenContent` with the standard Loading/Empty/Content states. Pure-read, no
 * actions — the local-only Room source has no error or network states to model.
 */
class PersonalWatchlistViewModel(
    repository: WatchlistRepository,
) : BaseViewModel<Unit, Nothing, Nothing>(Unit) {

    val screenState: StateFlow<ScreenState<List<WatchlistItem>>> = repository.watchlist()
        .map { items ->
            if (items.isEmpty()) {
                ScreenState.Empty
            } else {
                ScreenState.Content(data = items, freshness = DataFreshness.FRESH)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState.Loading)

    override fun handleAction(action: Nothing) {}
}
