/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.currency

import kotlinx.coroutines.test.runTest
import kpt.core.data.InMemoryFetchedAtRepository
import kpt.core.data.RecordingSynchronizer
import kpt.core.data.currency.impl.CurrencyRepositoryImpl
import kpt.core.data.onlineNetworkMonitor
import kpt.core.model.currency.ExchangeRates
import kpt.core.model.currency.RateHistory
import kpt.core.model.currency.RateHistoryKey
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.StoreBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * AC-28 / AC-31d — `CurrencyRepositoryImpl.syncWith` is the D21 forced-refresh
 * seam: for each pinned base currency it re-subscribes to `exchangeRatesStream`
 * under `FetchPolicy.NETWORK_ONLY`, which drives the Store5 fetcher (a real
 * network hit) and writes through SourceOfTruth. The test proves the fetcher is
 * invoked once per pinned base and that a fresh sync timestamp is stamped.
 */
class CurrencyRepositorySyncWithTest {

    @Test
    fun syncWith_forces_a_network_fetch_for_every_pinned_base_currency() = runTest {
        val fetchedBases = mutableListOf<String>()

        // NETWORK_ONLY drives this fetcher; recording proves the forced refresh happened.
        val exchangeRatesStore = StoreBuilder
            .from(
                Fetcher.of<String, ExchangeRates> { base ->
                    fetchedBases += base
                    ExchangeRates(base = base, date = "2026-01-01", rates = mapOf("USD" to 1.0))
                },
            )
            .build()

        // Not exercised by syncWith — a placeholder store to satisfy the constructor.
        val rateHistoryStore = StoreBuilder
            .from(Fetcher.of<RateHistoryKey, RateHistory> { error("rateHistoryStore is not used by syncWith") })
            .build()

        val repo = CurrencyRepositoryImpl(
            exchangeRatesStore = exchangeRatesStore,
            rateHistoryStore = rateHistoryStore,
            networkMonitor = onlineNetworkMonitor(),
            fetchedAtRepository = InMemoryFetchedAtRepository(),
        )
        val synchronizer = RecordingSynchronizer()

        val result = repo.syncWith(synchronizer)

        assertTrue(result, "syncWith must succeed")
        assertEquals(
            setOf("USD", "EUR", "INR"),
            fetchedBases.toSet(),
            "every pinned base currency must trigger a network fetch",
        )
        assertTrue(
            synchronizer.stored.versions.containsKey("currency-rates"),
            "a fresh sync timestamp must be stamped under 'currency-rates'",
        )
    }
}
