/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.addtowatchlist.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.store.submit.SubmitState
import kpt.core.base.store.submit.submitHandler
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.demo.watchlist.WatchlistRepository

/**
 * The WRITE side of the Personal Crypto Watchlist — the canonical `submit_offline_write`
 * ("input simple" [SubmitHandler]) reference. Backs the embedded [AddToWatchlistStar] star
 * toggle hosted on `feature/crypto`'s coin surfaces (it is NOT a navigable destination).
 *
 * The [coinId] is injected via Koin `parametersOf(coinId)` (see `AddToWatchlistModule`) so
 * each hosting row gets its own VM keyed by coin.
 *
 * Two orthogonal streams the star renders from:
 * - [isTracked] — read side: `repository.contains(coinId)` (reactive `Flow<Boolean>`), drives
 *   filled (tracked) vs outline (not tracked). Re-emits instantly after a toggle because both
 *   DAO writes are wrapped in the core-base `notifyingWrite {}` invalidation bridge.
 * - [submitState] — write side: the [SubmitHandler] state (Idle / Submitting / Submitted / Failed).
 *   The local Room commit effectively never fails on-device; the Failed arm is the framework showcase.
 *
 * [onToggle] is the screen-facing API: not-tracked → `repository.add(coinId)`;
 * tracked → `repository.remove(coinId)`. `add()` is idempotent (REPLACE, addedAtMs=now);
 * `remove()` no-ops if absent.
 */
class AddToWatchlistViewModel(
    private val repository: WatchlistRepository,
    private val coinId: String,
) : BaseViewModel<Unit, Nothing, AddToWatchlistAction>(Unit) {

    private val toggleSubmitHandler = viewModelScope.submitHandler<Unit>()

    /** Read side — filled/outline. Seeded `false` until `contains()` first emits. */
    val isTracked: StateFlow<Boolean> = repository.contains(coinId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** Write side — Idle / Submitting / Submitted / Failed. */
    val submitState: StateFlow<SubmitState<Unit>> = toggleSubmitHandler.state

    override fun handleAction(action: AddToWatchlistAction) {
        when (action) {
            is AddToWatchlistAction.Toggle -> onToggle()
        }
    }

    /**
     * Toggle membership. Reads the CURRENT tracked value and routes to the opposite write —
     * both direct-DAO commits are wrapped in `notifyingWrite {}` so `contains()` re-emits and
     * the star flips. Idempotent both ways.
     */
    fun onToggle() {
        val tracked = isTracked.value
        toggleSubmitHandler.submit {
            if (tracked) repository.remove(coinId) else repository.add(coinId)
        }
    }
}

/** MVI action surface for the star toggle. */
sealed interface AddToWatchlistAction {
    /** Tap the star — add-or-remove based on the current `isTracked` value. */
    data object Toggle : AddToWatchlistAction
}
