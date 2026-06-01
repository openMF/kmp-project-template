/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.watchlist.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Persistent row representing a coin in the user's personal watchlist.
 *
 * Local-only: no Store5 caching layer, no remote sync. The user's watchlist
 * is a private device-local list of coin identifiers; full coin metadata
 * (name, symbol, price, etc.) is resolved on-demand via the existing
 * `coinDetailStream` / `coinMarketsStream` from CryptoRepository.
 *
 * @property coinId CoinGecko coin identifier (e.g., "bitcoin", "ethereum"). Primary key.
 * @property addedAtMs Epoch millis when the coin was added — used for default sort order.
 */
@Entity(tableName = "personal_watchlist")
data class WatchlistEntity(
    @PrimaryKey
    val coinId: String,
    val addedAtMs: Long,
)
