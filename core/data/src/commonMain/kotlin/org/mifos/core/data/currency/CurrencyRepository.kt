/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.data.currency

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import io.github.mobilebytelabs.worker.scheduler.sync.Syncable
import org.mifos.core.model.currency.ExchangeRates
import org.mifos.core.model.currency.RateHistory
import org.mifos.core.model.currency.RateHistoryKey
import template.core.base.store.screen.ScreenDataStream

interface CurrencyRepository : Syncable {
    fun exchangeRatesStream(baseCurrency: String, scope: CoroutineScope): ScreenDataStream<ExchangeRates>

    fun rateHistoryStream(keyFlow: Flow<RateHistoryKey>, scope: CoroutineScope): ScreenDataStream<RateHistory>
}
