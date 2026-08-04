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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.demo.watchlist.WatchlistItem
import kpt.core.data.demo.watchlist.WatchlistRepository

/**
 * Watchlist ViewModel — the canonical `read_local_list` demo (offline Room-backed
 * reactive LIST, non-paged; distinct from `read_paged_list` which is networked +
 * PageKey-paged). Reads flow straight from [WatchlistRepository.watchlist] — a plain
 * reactive [kotlinx.coroutines.flow.Flow], lifted into [ScreenState] (Empty when the
 * list is empty, Content otherwise). Removal is a one-shot local write; the read side
 * re-emits as soon as Room propagates the change.
 */
class WatchlistViewModel(
    private val repository: WatchlistRepository,
) : BaseViewModel<Unit, Nothing, WatchlistAction>(Unit) {

    /** Reactive watchlist state — `Empty` when zero coins are saved, else `Content`. */
    val screenState: StateFlow<ScreenState<List<WatchlistItem>>> =
        repository.watchlist()
            .map { items -> if (items.isEmpty()) ScreenState.Empty else ScreenState.Content(items) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: WatchlistAction) {
        when (action) {
            is WatchlistAction.Remove -> viewModelScope.launch { repository.remove(action.coinId) }
        }
    }

    /** Convenience emitter so the screen calls `onRemove(id)` instead of `trySendAction`. */
    fun onRemove(coinId: String) {
        trySendAction(WatchlistAction.Remove(coinId))
    }

    private companion object {
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

/** One-shot actions accepted by [WatchlistViewModel]. */
sealed interface WatchlistAction {
    /** Remove a coin from the watchlist. Idempotent — no-op if not present. */
    data class Remove(val coinId: String) : WatchlistAction
}
