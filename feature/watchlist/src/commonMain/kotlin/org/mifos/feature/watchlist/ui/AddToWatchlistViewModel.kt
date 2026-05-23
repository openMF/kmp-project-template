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

import kotlinx.coroutines.flow.Flow
import org.mifos.core.data.watchlist.WatchlistRepository
import template.core.base.ui.viewmodel.BaseMutationViewModel

/**
 * Toggle-write ViewModel for the watchlist star icon. Canonical `SubmitHandler` showcase.
 *
 * The submit lifecycle is driven by [BaseMutationViewModel] — calls flow through
 * `submitHandler.submit { ... }` so concurrent taps are serialized into Idle → Submitting
 * → Success/Failure transitions. The screen observes [uiState].submit for feedback
 * (loading spinner during submit, error overlay on failure) and [isInWatchlist] for the
 * icon variant (filled vs outline).
 *
 * @param coinId The coin identifier this button toggles. Injected via Koin parameters at
 *   the call site — one VM instance per coin currently being rendered.
 */
class AddToWatchlistViewModel(
    private val coinId: String,
    private val repository: WatchlistRepository,
) : BaseMutationViewModel<WatchlistMutation, Unit>() {

    /** Reactive in-membership for the current [coinId] — drives the star icon variant. */
    val isInWatchlist: Flow<Boolean> = repository.contains(coinId)

    override suspend fun performSubmit(payload: WatchlistMutation): Unit = when (payload) {
        WatchlistMutation.Add -> repository.add(coinId)
        WatchlistMutation.Remove -> repository.remove(coinId)
    }
}

/** Explicit toggle direction — avoids any ambiguity between Add and Remove. */
sealed interface WatchlistMutation {
    data object Add : WatchlistMutation
    data object Remove : WatchlistMutation
}
