/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.watchlist.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.demo.watchlist.WatchlistRepository
import kpt.core.model.demo.watchlist.WatchlistItem

/**
 * Watchlist ViewModel — the canonical `read_local_list` demo. A pure passthrough: it exposes the
 * repository's offline-local [WatchlistRepository.watchlistStream] ([ScreenDataStream]) directly and
 * the screen consumes it via `ScreenContent(stream = viewModel.watchlist)`; the stream owns the
 * state (its `isEmpty` yields Empty for zero coins). Removal is a one-shot local write; the read side
 * re-emits as soon as Room propagates.
 */
class WatchlistViewModel(
    private val repository: WatchlistRepository,
) : BaseViewModel<Unit, Nothing, WatchlistAction>(Unit) {

    /** The repository-built stream — the screen renders it directly. */
    val watchlist: ScreenDataStream<List<WatchlistItem>> = repository.watchlistStream(viewModelScope)

    override fun handleAction(action: WatchlistAction) {
        when (action) {
            is WatchlistAction.Remove -> viewModelScope.launch { repository.remove(action.coinId) }
        }
    }

    /** Convenience emitter so the screen calls `onRemove(id)` instead of `trySendAction`. */
    fun onRemove(coinId: String) {
        trySendAction(WatchlistAction.Remove(coinId))
    }
}

/** One-shot actions accepted by [WatchlistViewModel]. */
sealed interface WatchlistAction {
    /** Remove a coin from the watchlist. Idempotent — no-op if not present. */
    data class Remove(val coinId: String) : WatchlistAction
}
