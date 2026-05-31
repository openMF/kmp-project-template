package org.mifos.core.data.currency.impl

import io.github.mobilebytelabs.worker.WorkData
import io.github.mobilebytelabs.worker.workDataOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import io.github.mobilebytelabs.worker.scheduler.sync.Synchronizer
import io.github.mobilebytelabs.worker.scheduler.sync.snapshotSync
import org.mifos.core.data.currency.CurrencyRepository
import org.mifos.core.model.currency.ExchangeRates
import org.mifos.core.model.currency.RateHistory
import org.mifos.core.network.currency.api.FrankfurterApi

/**
 * Canonical OfflineFirst-shape adopter #1 (D20). Modeled on NiA's OfflineFirstTopicsRepository.
 *
 * Reads delegate to the existing currencyStore (template-merged via PR #160 consolidate-store-registry).
 * syncWith calls `Synchronizer.snapshotSync` (D21) — Frankfurter is a snapshot API, not a delta API.
 *
 * NO Dao reference in this file (D20 contract — writes route through Store5's SourceOfTruth).
 *
 * D23 amendment (2026-05-30): overrides BOTH 1-arg + 2-arg `syncWith`. 2-arg reads payload
 * keys `currency.base` (default USD) + `currency.symbols` (default SUPPORTED_SYMBOLS).
 */
class OfflineFirstCurrencyRepository(
    private val frankfurterApi: FrankfurterApi,
    // Phase 5 will wire the actual Store5 currencyStore from AppStoreRegistry.
    // Phase 3 leaves the store as an opaque write target for testability.
    private val currencyStore: CurrencyStoreWriteSurface,
) : CurrencyRepository {

    // Read paths — Phase 5 wires to the template's existing currencyStore.stream(...) pattern.
    // Left as TODO so the file compiles in isolation for tests of syncWith.
    override fun exchangeRatesStream(baseCurrency: String, scope: CoroutineScope) =
        TODO("Phase 5: delegate to currencyStore.stream(...) per existing template pattern")

    override fun rateHistoryStream(keyFlow: Flow<org.mifos.core.model.currency.RateHistoryKey>, scope: CoroutineScope) =
        TODO("Phase 5: delegate to currencyStore.stream(...) per existing template pattern")

    /** NiA-verbatim 1-arg variant — delegates to 2-arg with empty payload. */
    override suspend fun syncWith(synchronizer: Synchronizer): Boolean =
        syncWith(synchronizer, workDataOf())

    /**
     * D23 2-arg overload — reads adopter-specific payload keys.
     *
     * Keys recognized:
     *   - `currency.base`     (default "USD")
     *   - `currency.symbols`  (default SUPPORTED_SYMBOLS)
     */
    override suspend fun syncWith(synchronizer: Synchronizer, payload: WorkData): Boolean {
        val base = payload.getString("currency.base") ?: "USD"
        val symbols = payload.getString("currency.symbols") ?: SUPPORTED_SYMBOLS
        return synchronizer.snapshotSync(name = "currency-rates") {
            val rates: ExchangeRates = frankfurterApi.latest(base = base, symbols = symbols)
            currencyStore.write(base = base, rates = rates)
        }
    }

    private companion object {
        const val SUPPORTED_SYMBOLS = "EUR,GBP,JPY,INR,AUD,CAD,CHF,CNY"
    }
}

/**
 * Test seam — abstracts the Store5 write call so unit tests can verify it was invoked
 * with the expected base+rates without depending on the real Store5 builder.
 *
 * Phase 5 provides the actual implementation that bridges to `AppStoreRegistry.currencyStore`.
 */
interface CurrencyStoreWriteSurface {
    suspend fun write(base: String, rates: ExchangeRates)
}
