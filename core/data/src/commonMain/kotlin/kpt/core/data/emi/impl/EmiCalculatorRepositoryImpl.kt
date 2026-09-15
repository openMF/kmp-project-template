/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.emi.impl

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.emi.EmiCalculatorRepository
import kpt.core.model.emi.EmiResult
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.config.AppStoreIds
import kpt.core.store.emi.impl.EmiParams
import org.mobilenativefoundation.store.store5.Store

/**
 * Store5-backed impl of [EmiCalculatorRepository].
 *
 * `FetchPolicy.CACHE_ONLY` is deliberate: the "fetcher" is a pure local computation, so there
 * is no network leg to gate on and no freshness to revalidate. The Store's in-memory cache
 * makes a repeated parameter set free; a new one computes once.
 */
@RepositoryBinding(binds = EmiCalculatorRepository::class)
internal class EmiCalculatorRepositoryImpl(
    @FromStore(AppStoreIds.Emi) private val emiStore: Store<EmiParams, EmiResult>,
) : EmiCalculatorRepository {

    override fun emiStream(params: EmiParams, scope: CoroutineScope): ScreenDataStream<EmiResult> =
        emiStore.asScreenStream(
            key = params,
            cacheKey = AppCacheKeys.Emi.of(params.principal, params.ratePercent, params.tenureMonths),
            scope = scope,
            fetchPolicy = FetchPolicy.CACHE_ONLY,
        )
}
