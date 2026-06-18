/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.rates.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kpt.core.base.store.freshness.FreshnessSignal
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.economic.EconomicRatesRepository
import kpt.core.model.economic.InterestRateSeries
import kpt.core.store.economic.impl.InterestRateSeriesKey

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

    /**
     * Per-card freshness Flow — drives the [FreshnessIndicator] info icon.
     * Defaults to a one-shot Initial signal for test fakes that don't model
     * freshness; production implementation delegates to `ScreenDataStream.freshness`.
     */
    val freshness: Flow<FreshnessSignal> get() = flowOf(FreshnessSignal.initial())

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
            override val freshness: Flow<FreshnessSignal> = stream.freshness
            override fun refresh() = stream.refresh()
        }
    }
}
