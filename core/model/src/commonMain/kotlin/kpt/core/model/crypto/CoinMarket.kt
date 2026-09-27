/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.crypto

/**
 * One row of the coin market list — the fields a list item renders, nothing more.
 */
data class CoinMarket(
    /** CoinGecko's id — the key every other endpoint takes. */
    val id: String,
    /** Ticker, e.g. `btc`. Not unique across coins; never use it as a key. */
    val symbol: String,
    /** Display name as CoinGecko publishes it; not localised. */
    val name: String,
    /** Remote logo URL. */
    val imageUrl: String,
    /** Price in the requested vs-currency, not necessarily USD. */
    val currentPrice: Double,
    /** Market capitalisation, in the same vs-currency as [currentPrice]. */
    val marketCap: Long,
    /**
     * Rank by market cap, 1-based. Server-assigned and can jump between fetches, so sort by it rather than trusting
     * list order.
     */
    val marketCapRank: Int,
    /** 24h change as a percentage, already signed — negative means down. */
    val priceChangePercent24h: Double,
    /** 24h high. */
    val high24h: Double,
    /** 24h low. */
    val low24h: Double,
)

/**
 * A single coin's full detail, as shown on its own screen.
 */
data class CoinDetail(
    /** CoinGecko's coin id — the same value as [CoinMarket.id], so a list row and its detail share one key. */
    val id: String,
    /** Display name as CoinGecko publishes it. */
    val name: String,
    /** Ticker, e.g. `btc`. */
    val symbol: String,
    /** Remote logo URL. */
    val imageUrl: String,
    /** Price in the requested vs-currency. */
    val currentPrice: Double,
    /** Market capitalisation in the vs-currency this was fetched with. Whole units, not minor units. */
    val marketCap: Long,
    /**
     * Rank by market cap, 1-based. Carried on the detail model too so a detail screen reached from a deep link can
     * show the rank without also loading the list.
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
    /** Long-form description, HTML-stripped upstream. */
    val description: String,
)
