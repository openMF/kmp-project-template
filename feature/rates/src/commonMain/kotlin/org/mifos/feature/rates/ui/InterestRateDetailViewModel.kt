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

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.mifos.core.data.economic.EconomicRatesRepository
import org.mifos.core.model.economic.InterestRateSeries
import org.mifos.core.store.economic.impl.InterestRateSeriesKey
import template.core.base.store.screen.ScreenState
import template.core.base.ui.viewmodel.BaseViewModel

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

    private val stream = repository.interestRateSeriesStream(
        key = key,
        scope = viewModelScope,
    )

    val screenState: StateFlow<ScreenState<InterestRateSeries>> = stream.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState.Loading)

    /** Resolved key — exposed so the screen can show the human-readable series name. */
    val seriesKey: InterestRateSeriesKey get() = key

    fun onRetry() {
        trySendAction(DetailAction.Retry)
    }

    fun onRefresh() {
        trySendAction(DetailAction.Refresh)
    }

    override fun handleAction(action: DetailAction) = when (action) {
        DetailAction.Retry, DetailAction.Refresh -> stream.refresh()
    }
}

sealed interface DetailAction {
    data object Retry : DetailAction
    data object Refresh : DetailAction
}
