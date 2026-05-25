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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.mifos.core.data.economic.EconomicRatesRepository
import org.mifos.core.model.economic.InterestRateSeries
import org.mifos.core.store.economic.impl.InterestRateSeriesKey
import template.core.base.store.screen.ExperimentalScreenDataStreamTestingApi
import template.core.base.store.screen.ScreenDataStream
import template.core.base.store.screen.ScreenState
import template.core.base.store.screen.screenDataStreamForTesting

/**
 * Test double for [EconomicRatesRepository] that drives [InterestRateDetailViewModel]
 * through every state transition without going through Store5 / FRED.
 */
@OptIn(ExperimentalScreenDataStreamTestingApi::class)
internal class FakeEconomicRatesRepository : EconomicRatesRepository {

    val state: MutableStateFlow<ScreenState<InterestRateSeries>> =
        MutableStateFlow(ScreenState.Loading)
    val refreshTrigger: MutableSharedFlow<Unit> = MutableSharedFlow(extraBufferCapacity = 8)

    /** Keys that the ViewModel opened the stream with. */
    val openedKeys: MutableList<InterestRateSeriesKey> = mutableListOf()

    override fun interestRateSeriesStream(
        key: InterestRateSeriesKey,
        scope: CoroutineScope,
    ): ScreenDataStream<InterestRateSeries> {
        openedKeys += key
        return screenDataStreamForTesting(state = state, refreshTrigger = refreshTrigger)
    }

    override fun interestRateSeriesStream(
        keyFlow: Flow<InterestRateSeriesKey>,
        scope: CoroutineScope,
    ): ScreenDataStream<InterestRateSeries> =
        screenDataStreamForTesting(state = state, refreshTrigger = refreshTrigger)
}
