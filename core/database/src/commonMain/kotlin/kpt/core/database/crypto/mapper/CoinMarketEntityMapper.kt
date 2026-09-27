/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.crypto.mapper

import kpt.core.database.crypto.entity.CoinMarketEntity
import kpt.core.model.crypto.CoinMarket
import kotlin.time.Clock

/**
 * Domain → row, stamping the [page] this item belongs to so `replacePage` can swap one bucket.
 */
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

/**
 * Row → domain, dropping the paging bookkeeping.
 */
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
