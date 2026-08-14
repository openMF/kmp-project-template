/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.demo.exchange

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.screen.requireData
import kpt.core.model.demo.currency.ExchangeRates
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Store5 read-flavor contract test for the NETWORK_ONLY archetype that
 * [kpt.core.store.demo.exchange.impl.provideSpotRateLookupStore] implements
 * (`StoreFactory.createStore` over a Frankfurter fetcher + an `ExchangeRates`
 * SourceOfTruth). Resolves the previously-dead `SpotRateLookupStoreTest`
 * reference in the fork's CLAUDE.md "Store Archetype Showcases" table (AC7).
 *
 * The three cases are the canonical read-flavor contract from
 * `.claude/agents/kmp-store-gen.md` STEP 3:
 *  - cache hit  → serve from SourceOfTruth, never touch the fetcher
 *  - cache miss → invoke the fetcher, write through, emit, and persist
 *  - forced refresh (the NETWORK_ONLY semantic) → re-invoke the fetcher even
 *    when the SourceOfTruth already holds a (stale) value
 *
 * `provideSpotRateLookupStore` builds its store with `StoreFactory.createStore`,
 * which is `StoreBuilder.from(fetcher, sourceOfTruth).build()`. This test builds
 * the identical Store5 shape and additionally binds the store's internal scope to
 * the test scheduler (`.scope(backgroundScope)`) for deterministic virtual-time
 * driving — mirroring the fork's proven `CurrencyRepositorySyncWithTest` harness.
 * The NETWORK_ONLY archetype itself is a call-site fetch-policy decision
 * (`FetchPolicy.NETWORK_ONLY` → `StoreReadRequest.fresh`), asserted directly here.
 */
class SpotRateLookupStoreTest {

    @Test
    fun cache_hit_emits_from_sot_without_fetcher_call() = runTest {
        var fetcherCalls = 0
        val seeded = ratesFor("USD", 1.0)
        val sot = MutableStateFlow(mapOf("USD" to seeded))
        val store = spotRateStore(
            sot = sot,
            fetcher = { key ->
                fetcherCalls++
                ratesFor(key, 99.0) // any invocation would corrupt the cache-hit assertion
            },
        )

        val out = store
            .stream(StoreReadRequest.cached(key = "USD", refresh = false))
            .first { it is StoreReadResponse.Data<*> || it is StoreReadResponse.Error }
            .requireData()

        assertEquals(seeded, out, "a cache hit must serve the SourceOfTruth value verbatim")
        assertEquals(0, fetcherCalls, "a cache hit must NOT invoke the fetcher")
    }

    @Test
    fun cache_miss_calls_fetcher_persists_to_sot_emits() = runTest {
        var fetcherCalls = 0
        val fresh = ratesFor("EUR", 1.08)
        val sot = MutableStateFlow<Map<String, ExchangeRates>>(emptyMap())
        val store = spotRateStore(
            sot = sot,
            fetcher = { key ->
                fetcherCalls++
                if (key == "EUR") fresh else ratesFor(key, 0.0)
            },
        )

        val out = store
            .stream(StoreReadRequest.cached(key = "EUR", refresh = false))
            .first { it is StoreReadResponse.Data<*> || it is StoreReadResponse.Error }
            .requireData()

        assertEquals(fresh, out, "a cache miss must emit the freshly fetched value")
        assertEquals(1, fetcherCalls, "a cache miss must invoke the fetcher exactly once")
        assertEquals(
            fresh,
            sot.value["EUR"],
            "the fetched value must be written through to the SourceOfTruth (persistence)",
        )
    }

    @Test
    fun validator_expiry_triggers_refetch() = runTest {
        var fetcherCalls = 0
        val stale = ratesFor("GBP", 0.79)
        val networkValue = ratesFor("GBP", 0.80)
        // SourceOfTruth already holds a (stale) value — a plain cache read would serve it.
        val sot = MutableStateFlow(mapOf("GBP" to stale))
        val store = spotRateStore(
            sot = sot,
            fetcher = { key ->
                fetcherCalls++
                if (key == "GBP") networkValue else ratesFor(key, 0.0)
            },
        )

        // NETWORK_ONLY archetype semantic: FetchPolicy.NETWORK_ONLY maps to a forced-fresh
        // request, which re-invokes the fetcher even though the SourceOfTruth is populated.
        val out = store
            .stream(StoreReadRequest.fresh(key = "GBP"))
            .first { it is StoreReadResponse.Data<*> || it is StoreReadResponse.Error }
            .requireData()

        assertEquals(networkValue, out, "a forced refresh must serve the network value, not the stale cache")
        assertEquals(1, fetcherCalls, "a forced refresh must re-invoke the fetcher despite a populated SourceOfTruth")
    }
}

// ---------------------------------------------------------------------------
// Test harness — an in-memory Store5 read store matching the shape
// provideSpotRateLookupStore builds (StoreFactory.createStore = StoreBuilder
// .from(fetcher, sourceOfTruth).build()), bound to the test scheduler for
// deterministic virtual-time driving.
// ---------------------------------------------------------------------------

private fun ratesFor(base: String, usd: Double): ExchangeRates =
    ExchangeRates(base = base, date = "2026-01-01", rates = mapOf("USD" to usd))

private fun kotlinx.coroutines.test.TestScope.spotRateStore(
    sot: MutableStateFlow<Map<String, ExchangeRates>>,
    fetcher: suspend (String) -> ExchangeRates,
): Store<String, ExchangeRates> = StoreBuilder
    .from(
        fetcher = Fetcher.of { key: String -> fetcher(key) },
        sourceOfTruth = SourceOfTruth.of(
            reader = { key: String -> sot.map { it[key] } },
            writer = { key: String, value: ExchangeRates -> sot.value = sot.value + (key to value) },
            delete = { key: String -> sot.value = sot.value - key },
            deleteAll = { sot.value = emptyMap() },
        ),
    )
    .scope(backgroundScope)
    .build()
