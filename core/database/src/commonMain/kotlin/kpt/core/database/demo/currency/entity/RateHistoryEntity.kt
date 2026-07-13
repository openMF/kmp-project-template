/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.demo.currency.entity

import androidx.room3.Entity

@Entity(
    tableName = "rate_history",
    primaryKeys = ["fromCurrency", "toCurrency", "startDate", "endDate"],
)
data class RateHistoryEntity(
    val fromCurrency: String,
    val toCurrency: String,
    val startDate: String,
    val endDate: String,
    val ratesJson: String,
    val fetchedAt: Long,
)
