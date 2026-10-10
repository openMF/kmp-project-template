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
import kpt.core.model.crypto.CoinDetail

/**
 * Wire shape of CoinGecko's `/coins/{id}` response. Mapped to the domain model in the Store's
 * `SourceOfTruth`; nothing outside `core/network` should see this type.
 */
@Serializable
data class CoinDetailDto(
    /** CoinGecko's coin id. */
    val id: String,
    /** Display name as published upstream. */
    val name: String,
    /** Ticker, e.g. `btc`. */
    val symbol: String,
    /** Image URL set, or null when CoinGecko omits it. */
    val image: CoinImageDto? = null,
    /**
     * Price and market figures. Nullable because the endpoint can omit the block; [toDomain] substitutes zeroes rather
     * than failing the whole response.
     */
    @SerialName("market_data") val marketData: MarketDataDto? = null,
    /** Localised descriptions, of which only `en` is consumed. */
    val description: DescriptionDto? = null,
) {
    /**
     * Maps to the domain model, defaulting every absent figure to zero and the description to empty — a partial
     * response yields a renderable coin rather than an error.
     */
    fun toDomain(): CoinDetail = CoinDetail(
        id = id,
        name = name,
        symbol = symbol,
        imageUrl = image?.large.orEmpty(),
        currentPrice = marketData?.currentPrice?.get("usd") ?: 0.0,
        marketCap = marketData?.marketCap?.get("usd")?.toLong() ?: 0L,
        marketCapRank = marketData?.marketCapRank ?: 0,
        priceChangePercent24h = marketData?.priceChangePercentage24h ?: 0.0,
        high24h = marketData?.high24h?.get("usd") ?: 0.0,
        low24h = marketData?.low24h?.get("usd") ?: 0.0,
        circulatingSupply = marketData?.circulatingSupply ?: 0.0,
        maxSupply = marketData?.maxSupply,
        description = description?.en.orEmpty(),
    )
}

/**
 * Localised description block; only `en` is consumed.
 *
 * @property en English description, or null when absent.
 */
@Serializable
data class DescriptionDto(val en: String? = null)
