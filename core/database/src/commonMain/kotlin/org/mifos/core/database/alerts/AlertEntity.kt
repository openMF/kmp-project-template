/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.database.alerts

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Persistent row for a price alert.
 *
 * Stored in the `alerts` table (v9+). Each row represents a single
 * threshold-based alert for a given symbol.
 *
 * @property id Unique alert identifier.
 * @property symbol The ticker or instrument symbol this alert watches.
 * @property targetPrice The price level that triggers the alert.
 * @property conditionAbove `true` when the alert fires on price ≥ [targetPrice];
 *   `false` when it fires on price ≤ [targetPrice].
 * @property createdAt Creation timestamp in epoch-milliseconds.
 */
@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey val id: String,
    val symbol: String,
    val targetPrice: Double,
    val conditionAbove: Boolean,
    val createdAt: Long = 0L,
)
