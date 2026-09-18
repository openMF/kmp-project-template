/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.currency.mapper

import kotlinx.serialization.json.Json
import kpt.core.database.currency.entity.ExchangeRatesEntity
import kpt.core.model.currency.ExchangeRates
import kotlin.time.Clock

fun ExchangeRates.toEntity(baseCurrency: String): ExchangeRatesEntity = ExchangeRatesEntity(
    baseCurrency = baseCurrency,
    date = date,
    ratesJson = Json.encodeToString(rates),
    fetchedAt = Clock.System.now().toEpochMilliseconds(),
)

fun ExchangeRatesEntity.toDomain(): ExchangeRates = ExchangeRates(
    base = baseCurrency,
    date = date,
    rates = Json.decodeFromString(ratesJson),
)
