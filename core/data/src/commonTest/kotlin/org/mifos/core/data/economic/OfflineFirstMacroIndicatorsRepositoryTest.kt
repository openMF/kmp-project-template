package org.mifos.core.data.economic

import io.github.mobilebytelabs.worker.workDataOf
import kotlinx.coroutines.test.runTest
import org.mifos.core.data.infra.ChangeListVersions
import org.mifos.core.data.infra.Synchronizer
import org.mifos.core.data.economic.impl.MacroStoreWriteSurface
import org.mifos.core.data.economic.impl.OfflineFirstMacroIndicatorsRepository
import org.mifos.core.model.economic.Country
import org.mifos.core.model.economic.IndicatorKind
import org.mifos.core.model.economic.MacroIndicator
import org.mifos.core.network.economic.api.WorldBankApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OfflineFirstMacroIndicatorsRepositoryTest {
    @Test
    fun syncWith_iteratesCountriesIndicators_andWrites() = runTest {
        val sync = InMemorySync()
        val api = RecordingWorldBankApi()
        val store = RecordingMacroStore()
        val repo = OfflineFirstMacroIndicatorsRepository(
            worldBankApi = api,
            macroStore = store,
            countries = listOf(Country.US, Country.IN),
            indicators = listOf(IndicatorKind.GDP_USD, IndicatorKind.INFLATION_CPI),
        )
        val ok = repo.syncWith(sync)
        assertTrue(ok)
        assertEquals(4, store.writeCalls) // 2 countries × 2 indicators
        assertTrue((sync.getChangeListVersions().versions["macro-indicators"] ?: 0L) > 0L)
    }

    @Test
    fun syncWith_2arg_filtersCountries_byPayload() = runTest {
        val sync = InMemorySync()
        val api = RecordingWorldBankApi()
        val store = RecordingMacroStore()
        val repo = OfflineFirstMacroIndicatorsRepository(
            worldBankApi = api,
            macroStore = store,
            countries = listOf(Country.US, Country.IN, Country.GB),
            indicators = listOf(IndicatorKind.GDP_USD),
        )
        val ok = repo.syncWith(sync, workDataOf("macro.countries" to "US,IN"))
        assertTrue(ok)
        assertEquals(2, store.writeCalls) // 2 filtered countries × 1 indicator
        assertTrue(api.calls.all { it.first in setOf("US", "IN") })
    }
}

private class RecordingWorldBankApi : WorldBankApi {
    val calls = mutableListOf<Pair<String, String>>()
    override suspend fun indicator(countryCode: String, indicator: String): MacroIndicator {
        calls += countryCode to indicator
        return TODO("Construct a MacroIndicator stub using the template's actual constructor")
    }
}

private class RecordingMacroStore : MacroStoreWriteSurface {
    var writeCalls = 0
    override suspend fun write(country: Country, indicator: IndicatorKind, series: MacroIndicator) {
        writeCalls++
    }
}

private class InMemorySync(initial: ChangeListVersions = ChangeListVersions()) : Synchronizer {
    private var state = initial
    override suspend fun getChangeListVersions() = state
    override suspend fun updateChangeListVersions(update: ChangeListVersions.() -> ChangeListVersions) {
        state = state.update()
    }
}
