/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.model.currency

data class ExchangeRates(
    val base: String,
    val date: String,
    val rates: Map<String, Double>,
)

data class RateHistoryKey(
    val from: String,
    val to: String,
    val days: Int,
)

data class RateHistory(
    val from: String,
    val to: String,
    val startDate: String,
    val endDate: String,
    val rates: List<RatePoint>,
)

data class RatePoint(
    val date: String,
    val value: Double,
)
