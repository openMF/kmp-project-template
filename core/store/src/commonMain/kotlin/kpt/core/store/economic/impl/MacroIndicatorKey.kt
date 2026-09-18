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

import kpt.core.model.economic.IndicatorKind

/**
 * Composite key identifying a single World Bank macro-indicator request.
 *
 * @property countryCode ISO 3166-1 alpha-2 country code (e.g. `"US"`).
 * @property indicator Which macro series to fetch — see [IndicatorKind].
 * @property years Number of trailing calendar years to request. Default 25
 *   gives a comfortable trend line for charts.
 */
data class MacroIndicatorKey(
    val countryCode: String,
    val indicator: IndicatorKind,
    val years: Int = 25,
)
