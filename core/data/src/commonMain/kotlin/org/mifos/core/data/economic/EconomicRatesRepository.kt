/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.data.economic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.mifos.core.model.economic.InterestRateSeries
import org.mifos.core.store.economic.impl.InterestRateSeriesKey
import template.core.base.store.screen.ScreenDataStream

/**
 * Repository surface for FRED-sourced interest-rate time series.
 *
 * Consumes the `InterestRateSeries` Store5 cache under the hood — callers get
 * a [ScreenDataStream] that emits loading/empty/error/content transitions
 * automatically (per the toolkit's offline-first store contract).
 */
interface EconomicRatesRepository {

    /**
     * Stream observations for a single static series.
     *
     * @param key Identifier + display metadata for the FRED series.
     * @param scope Lifecycle scope that bounds the underlying cold flow.
     */
    fun interestRateSeriesStream(
        key: InterestRateSeriesKey,
        scope: CoroutineScope,
    ): ScreenDataStream<InterestRateSeries>

    /**
     * Stream observations for a parameter-flow whose value can change at
     * runtime (e.g. user switches series in the UI). Each new emission on
     * [keyFlow] triggers a fresh load (cache-first).
     */
    fun interestRateSeriesStream(
        keyFlow: Flow<InterestRateSeriesKey>,
        scope: CoroutineScope,
    ): ScreenDataStream<InterestRateSeries>
}
