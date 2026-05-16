/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.database.currency.mapper

import kotlinx.serialization.json.Json
import org.mifos.core.database.currency.RatePointPair
import org.mifos.core.database.currency.entity.RateHistoryEntity
import org.mifos.core.model.currency.RateHistory
import org.mifos.core.model.currency.RatePoint
import kotlin.time.Clock

fun RateHistory.toEntity(): RateHistoryEntity = RateHistoryEntity(
    fromCurrency = from,
    toCurrency = to,
    startDate = startDate,
    endDate = endDate,
    ratesJson = Json.encodeToString(rates.map { RatePointPair(it.date, it.value) }),
    fetchedAt = Clock.System.now().toEpochMilliseconds(),
)

fun RateHistoryEntity.toDomain(): RateHistory = RateHistory(
    from = fromCurrency,
    to = toCurrency,
    startDate = startDate,
    endDate = endDate,
    rates = Json.decodeFromString<List<RatePointPair>>(ratesJson)
        .map { RatePoint(it.date, it.value) },
)
