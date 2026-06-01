/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.crypto

import kotlinx.coroutines.CoroutineScope
import kpt.core.model.crypto.CoinDetail
import kpt.core.model.crypto.CoinMarket
import kpt.core.base.store.paging.PagingScreenStream
import kpt.core.base.store.screen.ScreenDataStream

interface CryptoRepository {
    fun coinMarketsStream(scope: CoroutineScope, pageSize: Int = 20): PagingScreenStream<CoinMarket>

    fun coinDetailStream(coinId: String, scope: CoroutineScope): ScreenDataStream<CoinDetail>
}
