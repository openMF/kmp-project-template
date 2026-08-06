/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.demo.watchlist

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.demo.watchlist.WatchlistItem

/**
 * User's personal watchlist of coins — purely local persistence, no remote sync.
 *
 * Demonstrates the "input simple" archetype: writes go through
 * [kpt.core.data.demo.watchlist.WatchlistRepository.add] /
 * [kpt.core.data.demo.watchlist.WatchlistRepository.remove], driven from
 * `AddToWatchlistViewModel` via `SubmitHandler` (the canonical simple-mutation
 * pattern). Reads are reactive [Flow]s straight from the DAO.
 */
interface WatchlistRepository {

    /** Observe the watchlist as a Store5-backed [ScreenDataStream] (offline-local, newest-added first). */
    fun watchlistStream(scope: CoroutineScope): ScreenDataStream<List<WatchlistItem>>

    /** Reactive in-membership check. Used by the star toggle to render filled/outline. */
    fun contains(coinId: String): Flow<Boolean>

    /** Add a coin. Idempotent — re-adds touch the addedAtMs timestamp to "now". */
    suspend fun add(coinId: String)

    /** Remove a coin. Idempotent — no-op if not present. */
    suspend fun remove(coinId: String)
}
