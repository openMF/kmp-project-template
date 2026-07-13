/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.demo.crypto.mapper

import kpt.core.database.demo.crypto.entity.CoinMarketEntity
import kpt.core.model.demo.crypto.CoinMarket
import kotlin.time.Clock

fun CoinMarket.toEntity(page: Int): CoinMarketEntity = CoinMarketEntity(
    id = id,
    symbol = symbol,
    name = name,
    imageUrl = imageUrl,
    currentPrice = currentPrice,
    marketCap = marketCap,
    marketCapRank = marketCapRank,
    priceChangePercent24h = priceChangePercent24h,
    high24h = high24h,
    low24h = low24h,
    page = page,
    fetchedAt = Clock.System.now().toEpochMilliseconds(),
)

fun CoinMarketEntity.toDomain(): CoinMarket = CoinMarket(
    id = id,
    symbol = symbol,
    name = name,
    imageUrl = imageUrl,
    currentPrice = currentPrice,
    marketCap = marketCap,
    marketCapRank = marketCapRank,
    priceChangePercent24h = priceChangePercent24h,
    high24h = high24h,
    low24h = low24h,
)
