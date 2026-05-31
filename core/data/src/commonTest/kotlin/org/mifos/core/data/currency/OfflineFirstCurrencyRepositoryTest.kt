package org.mifos.core.data.currency

import io.github.mobilebytelabs.worker.workDataOf
import kotlinx.coroutines.test.runTest
import io.github.mobilebytelabs.worker.scheduler.sync.ChangeListVersions
import io.github.mobilebytelabs.worker.scheduler.sync.Synchronizer
import org.mifos.core.data.currency.impl.CurrencyStoreWriteSurface
import org.mifos.core.data.currency.impl.OfflineFirstCurrencyRepository
import org.mifos.core.model.currency.ExchangeRates
import org.mifos.core.network.currency.api.FrankfurterApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OfflineFirstCurrencyRepositoryTest {
    @Test
    fun syncWith_callsFrankfurterAndStore_andBumpsVersion() = runTest {
        val sync = InMemorySync()
        val api = RecordingFrankfurterApi(rates = stubRates("USD", 5))
        val store = RecordingStore()
        val repo = OfflineFirstCurrencyRepository(api, store)

        val ok = repo.syncWith(sync)

        assertTrue(ok)
        assertEquals("USD", api.lastBase)
        assertTrue(store.writeCalls == 1)
        assertEquals("USD", store.lastBase)
        assertTrue((sync.getChangeListVersions().versions["currency-rates"] ?: 0L) > 0L)
    }

    @Test
    fun syncWith_2arg_routesPayloadKeys_toFrankfurter() = runTest {
        val sync = InMemorySync()
        val api = RecordingFrankfurterApi(rates = stubRates("EUR", 3))
        val store = RecordingStore()
        val repo = OfflineFirstCurrencyRepository(api, store)

        val ok = repo.syncWith(sync, workDataOf(
            "currency.base" to "EUR",
            "currency.symbols" to "USD,GBP,JPY",
        ))

        assertTrue(ok)
        assertEquals("EUR", api.lastBase)
        assertEquals("USD,GBP,JPY", api.lastSymbols)
        assertEquals("EUR", store.lastBase)
    }
}

private fun stubRates(base: String, count: Int): ExchangeRates =
    TODO("Construct an ExchangeRates instance using the template's actual constructor; this stub keeps the test wiring shape clear")

private class RecordingFrankfurterApi(private val rates: ExchangeRates) : FrankfurterApi {
    var lastBase: String? = null
    var lastSymbols: String? = null
    override suspend fun latest(base: String, symbols: String?): ExchangeRates {
        lastBase = base
        lastSymbols = symbols
        return rates
    }
    // Other FrankfurterApi methods left as TODO — uncomment when wiring to real interface.
}

private class RecordingStore : CurrencyStoreWriteSurface {
    var writeCalls = 0
    var lastBase: String? = null
    override suspend fun write(base: String, rates: ExchangeRates) {
        writeCalls++
        lastBase = base
    }
}

private class InMemorySync(initial: ChangeListVersions = ChangeListVersions()) : Synchronizer {
    private var state = initial
    override suspend fun getChangeListVersions() = state
    override suspend fun updateChangeListVersions(update: ChangeListVersions.() -> ChangeListVersions) {
        state = state.update()
    }
}
