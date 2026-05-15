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
import org.mifos.core.model.fintech.CoinDetail
import org.mifos.core.model.fintech.CoinMarket
import template.core.base.store.paging.PagingScreenStream
import template.core.base.store.screen.ScreenDataStream

interface CryptoRepository {
    fun coinMarketsStream(
        scope: CoroutineScope,
        pageSize: Int = 20,
    ): PagingScreenStream<CoinMarket>

    fun coinDetailStream(
        coinId: String,
        scope: CoroutineScope,
    ): ScreenDataStream<CoinDetail>
}
