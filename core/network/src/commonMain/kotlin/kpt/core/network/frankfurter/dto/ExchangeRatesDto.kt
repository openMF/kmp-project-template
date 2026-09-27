/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.frankfurter.dto

import kotlinx.serialization.Serializable
import kpt.core.model.currency.ExchangeRates

/**
 * Wire shape of the Frankfurter `/latest` response — base currency plus a rate map.
 */
@Serializable
data class ExchangeRatesDto(
    /** The amount the rates are quoted for — always 1 for `/latest`, so it is not mapped to the domain. */
    val amount: Double,
    /** Base currency code. */
    val base: String,
    /** The day these rates are for, `YYYY-MM-DD`. */
    val date: String,
    /** Quote-code → rate against [base]. */
    val rates: Map<String, Double>,
) {
    /** Maps to the domain model, dropping [amount]. */
    fun toDomain(): ExchangeRates = ExchangeRates(
        base = base,
        date = date,
        rates = rates,
    )
}
