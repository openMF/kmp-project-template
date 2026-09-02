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

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.store.freshness.FreshnessSignal
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.demo.economic.EconomicRatesRepository
import kpt.core.model.demo.economic.InterestRateSeries
import kpt.core.store.demo.economic.impl.InterestRateSeriesKey

/**
 * ViewModel for the per-series detail screen. Streams the full 365-day window
 * for [seriesId] through the same NETWORK_WITH_CACHE Store5 path used by the
 * list screen.
 *
 * @param seriesId FRED series identifier — must match a [RateSeriesCatalog] entry.
 *   Unknown ids fall back to a minimal `InterestRateSeriesKey(seriesId)` so the
 *   detail screen still renders something rather than crashing.
 */
internal class InterestRateDetailViewModel(
    seriesId: String,
    repository: EconomicRatesRepository,
) : BaseViewModel<Unit, Nothing, DetailAction>(Unit) {

    private val key: InterestRateSeriesKey = RateSeriesCatalog.findBySeriesId(seriesId)
        ?: InterestRateSeriesKey(seriesId = seriesId, name = seriesId)

    /** The repository-built stream — the screen renders it directly via `ScreenContent(stream)`. */
    val series: ScreenDataStream<InterestRateSeries> = repository.interestRateSeriesStream(
        key = key,
        scope = viewModelScope,
    )

    /**
     * Per-screen freshness — drives the TopAppBar [FreshnessIndicator] info icon.
     * Pure time-based staleness; network connectivity is rendered separately by
     * the global `ConnectivityBanner`.
     */
    val freshness: StateFlow<FreshnessSignal> = series.freshness
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FreshnessSignal.initial())

    /** Resolved key — exposed so the screen can show the human-readable series name. */
    val seriesKey: InterestRateSeriesKey get() = key

    fun onRetry() {
        trySendAction(DetailAction.Retry)
    }

    fun onRefresh() {
        trySendAction(DetailAction.Refresh)
    }

    // CACHE_FIRST_SWR default (see InterestRatesViewModel) — a user refresh must reach the network.
    override fun handleAction(action: DetailAction) = when (action) {
        DetailAction.Retry, DetailAction.Refresh -> series.refresh(forceFresh = true)
    }
}

sealed interface DetailAction {
    data object Retry : DetailAction
    data object Refresh : DetailAction
}
