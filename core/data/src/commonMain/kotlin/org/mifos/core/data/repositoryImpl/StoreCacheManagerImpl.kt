/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.data.repositoryImpl

import co.touchlab.kermit.Logger
import org.mifos.core.data.repository.StoreCacheManager
import org.mifos.core.database.dao.BookkeeperDao
import org.mifos.core.database.dao.DraftDao
import org.mifos.core.model.fintech.CoinDetail
import org.mifos.core.model.fintech.CoinMarket
import org.mifos.core.model.fintech.ExchangeRates
import org.mifos.core.model.fintech.RateHistory
import org.mifos.core.model.fintech.RateHistoryKey
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Store
import template.core.base.store.paging.PageKey

@OptIn(ExperimentalStoreApi::class)
class StoreCacheManagerImpl(
    private val exchangeRatesStore: Store<String, ExchangeRates>,
    private val rateHistoryStore: Store<RateHistoryKey, RateHistory>,
    private val coinMarketsStore: Store<PageKey, List<CoinMarket>>,
    private val coinDetailStore: Store<String, CoinDetail>,
    private val bookkeeperDao: BookkeeperDao,
    private val draftDao: DraftDao,
) : StoreCacheManager {

    override suspend fun clearAll() {
        Logger.d { "StoreCacheManager: clearing all store caches" }

        // Store.clear() clears both in-memory cache AND SourceOfTruth (deleteAll)
        exchangeRatesStore.clear()
        rateHistoryStore.clear()
        coinMarketsStore.clear()
        coinDetailStore.clear()

        // Clear bookkeeper sync-failure records
        bookkeeperDao.deleteAll()

        // Clear pending form drafts — prevents user A's drafts surfacing on user B's session
        draftDao.deleteAll()
    }
}
