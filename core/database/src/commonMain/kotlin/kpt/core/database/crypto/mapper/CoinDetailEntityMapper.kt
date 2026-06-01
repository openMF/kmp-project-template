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

import kpt.core.database.crypto.entity.CoinDetailEntity
import kpt.core.model.crypto.CoinDetail
import kotlin.time.Clock

fun CoinDetail.toEntity(): CoinDetailEntity = CoinDetailEntity(
    id = id,
    name = name,
    symbol = symbol,
    imageUrl = imageUrl,
    currentPrice = currentPrice,
    marketCap = marketCap,
    marketCapRank = marketCapRank,
    priceChangePercent24h = priceChangePercent24h,
    high24h = high24h,
    low24h = low24h,
    circulatingSupply = circulatingSupply,
    maxSupply = maxSupply,
    description = description,
    fetchedAt = Clock.System.now().toEpochMilliseconds(),
)

fun CoinDetailEntity.toDomain(): CoinDetail = CoinDetail(
    id = id,
    name = name,
    symbol = symbol,
    imageUrl = imageUrl,
    currentPrice = currentPrice,
    marketCap = marketCap,
    marketCapRank = marketCapRank,
    priceChangePercent24h = priceChangePercent24h,
    high24h = high24h,
    low24h = low24h,
    circulatingSupply = circulatingSupply,
    maxSupply = maxSupply,
    description = description,
)
