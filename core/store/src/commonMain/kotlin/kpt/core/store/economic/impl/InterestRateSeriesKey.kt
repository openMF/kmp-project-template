/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.economic.impl

/**
 * Composite key identifying a single FRED series request. FRED's
 * `series/observations` endpoint does not echo the human-readable name or unit
 * of measure in its response, so the caller threads both through the key so the
 * domain object can carry them.
 *
 * @property seriesId FRED series identifier.
 * @property name Human-readable display label.
 * @property unit Unit of measure (`"%"`, `"USD"`, …).
 * @property days Window size in days — observations cover `[today - days, today]`.
 */
data class InterestRateSeriesKey(
    val seriesId: String,
    val name: String,
    val unit: String = "%",
    val days: Int = 365,
)
