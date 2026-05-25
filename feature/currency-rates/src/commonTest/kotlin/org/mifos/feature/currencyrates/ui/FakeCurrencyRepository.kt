/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.currencyrates.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.mifos.core.data.currency.CurrencyRepository
import org.mifos.core.model.currency.ExchangeRates
import org.mifos.core.model.currency.RateHistory
import org.mifos.core.model.currency.RateHistoryKey
import template.core.base.store.screen.ExperimentalScreenDataStreamTestingApi
import template.core.base.store.screen.ScreenDataStream
import template.core.base.store.screen.ScreenState
import template.core.base.store.screen.screenDataStreamForTesting

/**
 * Test double for [CurrencyRepository] that exposes the underlying mutable state
 * flows + refresh trigger so tests can drive the ViewModel through every state
 * transition (Loading → Content → Error → NoNetwork).
 */
@OptIn(ExperimentalScreenDataStreamTestingApi::class)
internal class FakeCurrencyRepository : CurrencyRepository {

    val exchangeRatesState: MutableStateFlow<ScreenState<ExchangeRates>> =
        MutableStateFlow(ScreenState.Loading)
    val exchangeRatesRefresh: MutableSharedFlow<Unit> =
        MutableSharedFlow(extraBufferCapacity = 8)

    val rateHistoryState: MutableStateFlow<ScreenState<RateHistory>> =
        MutableStateFlow(ScreenState.Loading)
    val rateHistoryRefresh: MutableSharedFlow<Unit> =
        MutableSharedFlow(extraBufferCapacity = 8)

    /** Latest base currency the ViewModel asked for — assert on this in tests. */
    var lastExchangeRatesBase: String? = null
        private set

    /** Snapshot of the latest [RateHistoryKey] the ViewModel emitted into the stream factory. */
    val rateHistoryKeys: MutableList<RateHistoryKey> = mutableListOf()

    override fun exchangeRatesStream(
        baseCurrency: String,
        scope: CoroutineScope,
    ): ScreenDataStream<ExchangeRates> {
        lastExchangeRatesBase = baseCurrency
        return screenDataStreamForTesting(
            state = exchangeRatesState,
            refreshTrigger = exchangeRatesRefresh,
        )
    }

    override fun rateHistoryStream(
        keyFlow: Flow<RateHistoryKey>,
        scope: CoroutineScope,
    ): ScreenDataStream<RateHistory> {
        // Drain key emissions onto a list so tests can assert the keys the VM produces.
        keyFlow.onEach { rateHistoryKeys += it }.launchIn(scope)
        return screenDataStreamForTesting(
            state = rateHistoryState,
            refreshTrigger = rateHistoryRefresh,
        )
    }
}
