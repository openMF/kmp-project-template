/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.rates.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.mifos.core.data.economic.EconomicRatesRepository
import org.mifos.core.model.economic.InterestRateSeries
import org.mifos.core.store.economic.impl.InterestRateSeriesKey
import template.core.base.store.screen.ScreenState

/**
 * Per-series stream + refresh seam used by [InterestRatesViewModel].
 *
 * Why an interface rather than calling `EconomicRatesRepository` directly?
 * The framework's `ScreenDataStream<T>` has an `internal` constructor — feature
 * modules can obtain instances but cannot fake them in unit tests. This thin
 * seam moves the stream + refresh contract into the feature module so the test
 * can swap in a `Flow`-backed fake without touching framework internals.
 *
 * The production implementation is [DefaultRateStreamFactory] which delegates
 * straight to the framework repository.
 */
internal interface RateStreamFactory {
    /**
     * Open a per-key stream of [ScreenState] transitions. The returned handle
     * exposes a [RateStream.state] flow and a [RateStream.refresh] entry point
     * for pull-to-refresh / row-retry.
     */
    fun open(key: InterestRateSeriesKey, scope: CoroutineScope): RateStream
}

/** Per-series stream handle returned by [RateStreamFactory.open]. */
internal interface RateStream {
    val state: Flow<ScreenState<InterestRateSeries>>
    fun refresh()
}

/**
 * Production [RateStreamFactory] — defers entirely to [EconomicRatesRepository]
 * which under the hood is a Store5-backed `ScreenDataStream`.
 */
internal class DefaultRateStreamFactory(
    private val repository: EconomicRatesRepository,
) : RateStreamFactory {
    override fun open(key: InterestRateSeriesKey, scope: CoroutineScope): RateStream {
        val stream = repository.interestRateSeriesStream(key = key, scope = scope)
        return object : RateStream {
            override val state: Flow<ScreenState<InterestRateSeries>> = stream.state
            override fun refresh() = stream.refresh()
        }
    }
}
