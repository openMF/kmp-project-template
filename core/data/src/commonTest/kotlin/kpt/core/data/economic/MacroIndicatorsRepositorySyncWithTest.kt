/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.economic

import kotlinx.coroutines.test.runTest
import kpt.core.data.InMemoryFetchedAtRepository
import kpt.core.data.RecordingSynchronizer
import kpt.core.data.economic.impl.MacroIndicatorsRepositoryImpl
import kpt.core.data.onlineNetworkMonitor
import kpt.core.model.economic.IndicatorKind
import kpt.core.model.economic.MacroIndicator
import kpt.core.store.economic.impl.MacroIndicatorKey
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.StoreBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * AC-29 / AC-31e — `MacroIndicatorsRepositoryImpl.syncWith` mirrors the currency
 * adopter: for each pinned (country, indicator) pair it re-subscribes to
 * `macroIndicatorStream` under `FetchPolicy.NETWORK_ONLY` (D21) and stamps a
 * fresh sync timestamp. Proves the World Bank fetcher fires once per pinned key.
 */
class MacroIndicatorsRepositorySyncWithTest {

    @Test
    fun syncWith_forces_a_network_fetch_for_every_pinned_macro_key() = runTest {
        val fetchedKeys = mutableListOf<MacroIndicatorKey>()

        val macroIndicatorStore = StoreBuilder
            .from(
                Fetcher.of<MacroIndicatorKey, MacroIndicator> { key ->
                    fetchedKeys += key
                    MacroIndicator(
                        countryCode = key.countryCode,
                        countryName = key.countryCode,
                        indicator = key.indicator,
                        observations = emptyList(),
                    )
                },
            )
            .build()

        val repo = MacroIndicatorsRepositoryImpl(
            macroIndicatorStore = macroIndicatorStore,
            networkMonitor = onlineNetworkMonitor(),
            fetchedAtRepository = InMemoryFetchedAtRepository(),
        )
        val synchronizer = RecordingSynchronizer()

        val result = repo.syncWith(synchronizer)

        assertTrue(result, "syncWith must succeed")
        assertEquals(
            setOf("US" to IndicatorKind.GDP, "IN" to IndicatorKind.GDP),
            fetchedKeys.map { it.countryCode to it.indicator }.toSet(),
            "every pinned (country, indicator) pair must trigger a network fetch",
        )
        assertTrue(
            synchronizer.stored.versions.containsKey("macro-indicators"),
            "a fresh sync timestamp must be stamped under 'macro-indicators'",
        )
    }
}
