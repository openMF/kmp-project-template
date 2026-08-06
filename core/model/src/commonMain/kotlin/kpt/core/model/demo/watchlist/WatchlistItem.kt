/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.demo.watchlist

/**
 * Domain model for a personal-watchlist row (the `read_local_list` demo). Lives in `core/model`
 * so the `core/store` `WatchlistStore` can emit it as its `SourceOfTruth` output (read-path
 * contract — the Store emits a DOMAIN model, never a Room `*Entity`).
 *
 * @property coinId CoinGecko identifier (look up details via CryptoRepository).
 * @property addedAtMs Epoch millis when the user added the coin.
 */
data class WatchlistItem(
    val coinId: String,
    val addedAtMs: Long,
)
