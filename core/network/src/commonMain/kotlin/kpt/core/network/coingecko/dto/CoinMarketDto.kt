/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.coingecko.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kpt.core.model.crypto.CoinMarket

/**
 * One row of CoinGecko's `/coins/markets` response — the paged market list.
 */
@Serializable
data class CoinMarketDto(
    /** CoinGecko's coin id. */
    val id: String,
    /** Ticker, e.g. `btc`. */
    val symbol: String,
    /** Display name as published upstream. */
    val name: String,
    /** Logo URL. */
    val image: String,
    /** Price in the requested vs-currency. */
    @SerialName("current_price") val currentPrice: Double,
    /** Market capitalisation in the requested vs-currency. Non-null here, unlike on the detail endpoint. */
    @SerialName("market_cap") val marketCap: Long,
    /** Rank by market cap, 1-based. Non-null on this endpoint. */
    @SerialName("market_cap_rank") val marketCapRank: Int,
    /** 24h change as a signed percentage, or null for a newly listed coin. */
    @SerialName("price_change_percentage_24h") val priceChangePercentage24h: Double? = null,
    /** 24h high, or null when there is not yet a full day of data. */
    @SerialName("high_24h") val high24h: Double? = null,
    /** 24h low, or null when there is not yet a full day of data. */
    @SerialName("low_24h") val low24h: Double? = null,
) {
    /**
     * Maps to the domain model. A missing 24h high/low falls back to [currentPrice] — a flat line is truthful for a
     * coin with no range yet, where a zero would render as a 100% crash.
     */
    fun toDomain(): CoinMarket = CoinMarket(
        id = id,
        symbol = symbol,
        name = name,
        imageUrl = image,
        currentPrice = currentPrice,
        marketCap = marketCap,
        marketCapRank = marketCapRank,
        priceChangePercent24h = priceChangePercentage24h ?: 0.0,
        high24h = high24h ?: currentPrice,
        low24h = low24h ?: currentPrice,
    )
}

/**
 * Image URL set for a coin; only the large variant is used.
 *
 * @property large large-size logo URL, or null when absent.
 */
@Serializable
data class CoinImageDto(val large: String? = null)

/**
 * Nested market figures (price, market cap, 24h change) inside a coin detail response.
 */
@Serializable
data class MarketDataDto(
    /** Price per vs-currency code. */
    @SerialName("current_price") val currentPrice: Map<String, Double>? = null,
    /** Market capitalisation per vs-currency code. */
    @SerialName("market_cap") val marketCap: Map<String, Double>? = null,
    /** Rank by market cap, 1-based, or null for a coin CoinGecko has not ranked yet. */
    @SerialName("market_cap_rank") val marketCapRank: Int? = null,
    /** 24h change as a signed percentage, or null when the coin has under a day of history. */
    @SerialName("price_change_percentage_24h") val priceChangePercentage24h: Double? = null,
    /** 24h high per vs-currency code. */
    @SerialName("high_24h") val high24h: Map<String, Double>? = null,
    /** 24h low per vs-currency code. */
    @SerialName("low_24h") val low24h: Map<String, Double>? = null,
    /** Coins in circulation. */
    @SerialName("circulating_supply") val circulatingSupply: Double? = null,
    /** Hard cap, or null for a coin with no fixed supply. */
    @SerialName("max_supply") val maxSupply: Double? = null,
)
