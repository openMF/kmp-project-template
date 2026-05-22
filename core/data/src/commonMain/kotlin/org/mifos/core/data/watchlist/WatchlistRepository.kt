/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.data.watchlist

import kotlinx.coroutines.flow.Flow

/**
 * User's personal watchlist of coins — purely local persistence, no remote sync.
 *
 * Demonstrates the "input simple" archetype: writes go through
 * [org.mifos.core.data.watchlist.WatchlistRepository.add] /
 * [org.mifos.core.data.watchlist.WatchlistRepository.remove], driven from
 * `AddToWatchlistViewModel` via `SubmitHandler` (the canonical simple-mutation
 * pattern). Reads are reactive [Flow]s straight from the DAO.
 */
interface WatchlistRepository {

    /** Observe the full watchlist (newest-added first). Reactive — emits on every change. */
    fun watchlist(): Flow<List<WatchlistItem>>

    /** Reactive in-membership check. Used by the star toggle to render filled/outline. */
    fun contains(coinId: String): Flow<Boolean>

    /** Add a coin. Idempotent — re-adds touch the addedAtMs timestamp to "now". */
    suspend fun add(coinId: String)

    /** Remove a coin. Idempotent — no-op if not present. */
    suspend fun remove(coinId: String)
}

/**
 * UI-facing projection of a watchlist row.
 *
 * @property coinId CoinGecko identifier (look up details via CryptoRepository).
 * @property addedAtMs Epoch millis when the user added the coin.
 */
data class WatchlistItem(
    val coinId: String,
    val addedAtMs: Long,
)
