/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.crypto.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kpt.core.base.database.annotation.DbEntity

/**
 * Room row for a cached coin detail.
 *
 * An ENTITY, not the domain model — the entity↔domain mapping lives in the Store's
 * `SourceOfTruth`, so nothing above `core/store` sees this type.
 */
@DbEntity
@Entity(tableName = "coin_detail")
data class CoinDetailEntity(
    /**
     * CoinGecko's coin id, e.g. `bitcoin`. Primary key, and the key every other endpoint takes — never the ticker,
     * which is not unique across coins.
     */
    @PrimaryKey
    val id: String,
    /** Display name as CoinGecko publishes it; not localised. */
    val name: String,
    /** Ticker, e.g. `btc`. */
    val symbol: String,
    /** Remote logo URL. */
    val imageUrl: String,
    /** Price in the requested vs-currency. */
    val currentPrice: Double,
    /**
     * Market capitalisation in the vs-currency the row was fetched with. A Long, so it is whole units of that currency
     * — not minor units.
     */
    val marketCap: Long,
    /**
     * Rank by market cap, 1-based. Server-assigned, so it can jump between fetches; sort by it rather than assuming it
     * matches row order.
     */
    val marketCapRank: Int,
    /** 24h change as a signed percentage. */
    val priceChangePercent24h: Double,
    /** 24h high. */
    val high24h: Double,
    /** 24h low. */
    val low24h: Double,
    /** Coins in circulation. */
    val circulatingSupply: Double,
    /** Hard cap, or null for a coin with no fixed supply. */
    val maxSupply: Double?,
    /** Long-form description. */
    val description: String,
    /** Epoch millis the row was cached. What the Store's TTL is measured from. */
    val fetchedAt: Long,
)
