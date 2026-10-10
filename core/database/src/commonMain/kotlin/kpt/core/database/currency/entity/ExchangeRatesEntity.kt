/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.currency.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kpt.core.base.database.annotation.DbEntity

/**
 * Room row for a cached FX rate set for one base currency.
 *
 * An ENTITY, not the domain model — the entity↔domain mapping lives in the Store's
 * `SourceOfTruth`, so nothing above `core/store` sees this type.
 */
@DbEntity
@Entity(tableName = "exchange_rates")
data class ExchangeRatesEntity(
    /** Base currency code, e.g. `USD`. Primary key. */
    @PrimaryKey
    val baseCurrency: String,
    /** The day these rates are for, `YYYY-MM-DD`. */
    val date: String,
    /**
     * Quote-code → rate, as JSON. A map column rather than a rates table: the whole set is read and written as one
     * unit.
     */
    val ratesJson: String,
    /** Epoch millis the row was cached. What the TTL is measured from. */
    val fetchedAt: Long,
)
