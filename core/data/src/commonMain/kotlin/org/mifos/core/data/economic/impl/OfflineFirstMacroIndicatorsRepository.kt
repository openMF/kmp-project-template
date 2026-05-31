package org.mifos.core.data.economic.impl

import io.github.mobilebytelabs.worker.WorkData
import io.github.mobilebytelabs.worker.workDataOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import io.github.mobilebytelabs.worker.scheduler.sync.Synchronizer
import io.github.mobilebytelabs.worker.scheduler.sync.snapshotSync
import org.mifos.core.data.economic.MacroIndicatorsRepository
import org.mifos.core.data.economic.SupportedCountries
import org.mifos.core.model.economic.Country
import org.mifos.core.model.economic.IndicatorKind
import org.mifos.core.model.economic.MacroIndicator
import org.mifos.core.network.economic.api.WorldBankApi
import org.mifos.core.store.economic.impl.MacroIndicatorKey

/**
 * Canonical OfflineFirst adopter #2 (D20). Same shape as OfflineFirstCurrencyRepository.
 *
 * Iterates [countries] × [indicators] (defaults: SupportedCountries.all × GDP+INFLATION+UNEMPLOYMENT),
 * fetches each series via WorldBank API, writes to the macroStore.
 *
 * D23 payload keys recognized:
 *   - `macro.countries`  (comma-separated ISO codes; default = all)
 *   - `macro.indicators` (comma-separated indicator codes; default = constructor list)
 *
 * NO Dao reference in this file.
 */
class OfflineFirstMacroIndicatorsRepository(
    private val worldBankApi: WorldBankApi,
    private val macroStore: MacroStoreWriteSurface,
    private val countries: List<Country> = SupportedCountries.all,
    private val indicators: List<IndicatorKind> = listOf(
        IndicatorKind.GDP_USD,
        IndicatorKind.INFLATION_CPI,
        IndicatorKind.UNEMPLOYMENT,
    ),
) : MacroIndicatorsRepository {

    override fun macroIndicatorStream(key: MacroIndicatorKey, scope: CoroutineScope) =
        TODO("Phase 5: delegate to macroStore.stream(...) per existing template pattern")

    override fun macroIndicatorStream(keyFlow: Flow<MacroIndicatorKey>, scope: CoroutineScope) =
        TODO("Phase 5: delegate to macroStore.stream(...) per existing template pattern")

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean =
        syncWith(synchronizer, workDataOf())

    override suspend fun syncWith(synchronizer: Synchronizer, payload: WorkData): Boolean {
        val effectiveCountries = payload.getString("macro.countries")
            ?.split(",")
            ?.mapNotNull { code -> countries.firstOrNull { it.code == code.trim() } }
            ?.takeIf { it.isNotEmpty() }
            ?: countries

        val effectiveIndicators = payload.getString("macro.indicators")
            ?.split(",")
            ?.mapNotNull { code -> indicators.firstOrNull { it.code == code.trim() } }
            ?.takeIf { it.isNotEmpty() }
            ?: indicators

        return synchronizer.snapshotSync(name = "macro-indicators") {
            for (country in effectiveCountries) {
                for (indicator in effectiveIndicators) {
                    val series: MacroIndicator = worldBankApi.indicator(
                        countryCode = country.code,
                        indicator = indicator.code,
                    )
                    macroStore.write(country = country, indicator = indicator, series = series)
                }
            }
        }
    }
}

interface MacroStoreWriteSurface {
    suspend fun write(country: Country, indicator: IndicatorKind, series: MacroIndicator)
}
