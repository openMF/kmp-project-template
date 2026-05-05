/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.mifos.core.model.fintech.ExchangeRates
import org.mifos.core.model.fintech.RateHistory
import org.mifos.core.model.fintech.RateHistoryKey
import template.core.base.store.ScreenDataStream

interface CurrencyRepository {
    fun exchangeRatesStream(
        baseCurrency: String,
        scope: CoroutineScope,
    ): ScreenDataStream<ExchangeRates>

    fun rateHistoryStream(
        keyFlow: Flow<RateHistoryKey>,
        scope: CoroutineScope,
    ): ScreenDataStream<RateHistory>
}
