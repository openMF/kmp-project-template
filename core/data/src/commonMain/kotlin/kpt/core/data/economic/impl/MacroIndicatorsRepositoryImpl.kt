/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.economic.impl

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kpt.core.data.economic.MacroIndicatorsRepository
import kpt.core.model.economic.MacroIndicator
import kpt.core.store.economic.impl.MacroIndicatorKey
import org.mobilenativefoundation.store.store5.Store
import kpt.core.base.store.infra.FetchedAtRepository
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream

class MacroIndicatorsRepositoryImpl(
    private val macroIndicatorStore: Store<MacroIndicatorKey, MacroIndicator>,
    private val networkMonitor: NetworkMonitor,
    private val fetchedAtRepository: FetchedAtRepository,
) : MacroIndicatorsRepository {

    override fun macroIndicatorStream(
        key: MacroIndicatorKey,
        scope: CoroutineScope,
    ): ScreenDataStream<MacroIndicator> = macroIndicatorStore.asScreenStream(
        key = key,
        networkMonitor = networkMonitor,
        fetchedAtRepository = fetchedAtRepository,
        cacheKey = "economic:macro:${key.countryCode}:${key.indicator.name}:${key.years}y",
        scope = scope,
    )

    override fun macroIndicatorStream(
        keyFlow: Flow<MacroIndicatorKey>,
        scope: CoroutineScope,
    ): ScreenDataStream<MacroIndicator> = macroIndicatorStore.asScreenStream(
        keyFlow = keyFlow,
        networkMonitor = networkMonitor,
        fetchedAtRepository = fetchedAtRepository,
        cacheKeyFor = { key ->
            "economic:macro:${key.countryCode}:${key.indicator.name}:${key.years}y"
        },
        scope = scope,
    )
}
