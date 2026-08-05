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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.emptyIfContent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.demo.watchlist.WatchlistRepository
import kpt.core.model.demo.watchlist.WatchlistItem

/**
 * Watchlist ViewModel — the canonical `read_local_list` demo (offline Room-backed reactive LIST,
 * non-paged; distinct from `read_paged_list` which is networked + PageKey-paged). Consumes the
 * repository's offline-local [WatchlistRepository.watchlistStream]
 * ([kpt.core.base.store.screen.ScreenDataStream]) `.state` — the Store decided Loading/Content, so
 * the VM only marks the empty list. Removal is a one-shot local write; the read side re-emits as
 * soon as Room propagates the change.
 */
class WatchlistViewModel(
    private val repository: WatchlistRepository,
) : BaseViewModel<Unit, Nothing, WatchlistAction>(Unit) {

    private val stream = repository.watchlistStream(viewModelScope)

    /** Reactive watchlist state — `Empty` when zero coins are saved, else `Content`. */
    val screenState: StateFlow<ScreenState<List<WatchlistItem>>> = stream.state
        .emptyIfContent { it.isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    /** Re-run the read (no-op refresh for the offline-local store; wired for ScreenContent). */
    fun onRetry() = stream.retry()

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
