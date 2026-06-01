/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.home.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.data.banking.BillReminderRepository
import kpt.core.data.banking.LoanRepository
import kpt.core.data.currency.CurrencyRepository
import kpt.core.data.economic.EconomicRatesRepository
import kpt.core.model.banking.BillReminder
import kpt.core.model.banking.Loan
import kpt.core.model.currency.ExchangeRates
import kpt.core.model.currency.RateHistory
import kpt.core.model.currency.RateHistoryKey
import kpt.core.model.economic.InterestRateSeries
import kpt.core.store.economic.impl.InterestRateSeriesKey
import kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Focused archetype showcase test for the **PERIODIC(300_000L)** pattern in
 * [HomeViewModel].
 *
 * The home dashboard exchange-rate tile must auto-refresh every 5 minutes in the
 * background without requiring a user pull-to-refresh. This is implemented by
 * passing [FetchPolicy.PERIODIC] with [HomeViewModel.EXCHANGE_RATE_REFRESH_INTERVAL_MS]
 * when opening the exchange-rate stream.
 *
 * Full dashboard-state + fan-in tests (slots, aggregates, RefreshAll, RetryRates, etc.)
 * live in [HomeViewModelTest].
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalScreenDataStreamTestingApi::class)
class HomeDashboardViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * **Archetype showcase: PERIODIC(300_000L)**
     *
     * The exchange-rate tile MUST be opened with [FetchPolicy.PERIODIC] at the
     * canonical 5-minute (300 000 ms) cadence. Any other policy — in particular the
     * default [FetchPolicy.NETWORK_WITH_CACHE] — would require the user to manually
     * pull-to-refresh to see an updated FX rate, breaking the "ambient, always-fresh"
     * UX contract of the home dashboard.
     */
    @Test
    fun periodic_policy_is_configured() = runTest {
        val currency = FakeDashboardCurrencyRepository()
        buildViewModel(currency = currency)
        dispatcher.scheduler.advanceUntilIdle()

        val policy = currency.lastFetchPolicy
        assertTrue(
            policy is FetchPolicy.PERIODIC,
            "Exchange-rate tile must use FetchPolicy.PERIODIC — found: $policy",
        )
        assertEquals(
            HomeViewModel.EXCHANGE_RATE_REFRESH_INTERVAL_MS,
            (policy as FetchPolicy.PERIODIC).intervalMillis,
            "Periodic interval must be exactly ${HomeViewModel.EXCHANGE_RATE_REFRESH_INTERVAL_MS} ms (5 minutes).",
        )
    }

    // region helpers

    private fun buildViewModel(
        currency: FakeDashboardCurrencyRepository = FakeDashboardCurrencyRepository(),
    ): HomeViewModel = HomeViewModel(
        loanRepository = EmptyLoanRepository,
        billReminderRepository = EmptyBillReminderRepository,
        economicRatesRepository = LoadingEconomicRatesRepository,
        currencyRepository = currency,
    )

    // endregion
}

// region Minimal test doubles (file-private)

@OptIn(ExperimentalScreenDataStreamTestingApi::class)
private class FakeDashboardCurrencyRepository : CurrencyRepository {
    private val source = MutableStateFlow<ScreenState<ExchangeRates>>(ScreenState.Loading)
    var lastFetchPolicy: FetchPolicy? = null
        private set

    override fun exchangeRatesStream(
        baseCurrency: String,
        scope: CoroutineScope,
        fetchPolicy: FetchPolicy,
    ): ScreenDataStream<ExchangeRates> {
        lastFetchPolicy = fetchPolicy
        return screenDataStreamForTesting(
            state = source,
            refreshTrigger = MutableSharedFlow(extraBufferCapacity = 64),
        )
    }

    override fun rateHistoryStream(
        keyFlow: Flow<RateHistoryKey>,
        scope: CoroutineScope,
    ): ScreenDataStream<RateHistory> = throw UnsupportedOperationException(
        "Home dashboard does not subscribe to rate-history streams.",
    )
}

private object EmptyLoanRepository : LoanRepository {
    private val empty = MutableStateFlow<List<Loan>>(emptyList())
    override fun observeAll(): Flow<List<Loan>> = empty
    override fun observeById(id: String): Flow<Loan?> = throw UnsupportedOperationException()
    override suspend fun getById(id: String): Loan? = null
    override suspend fun upsert(loan: Loan) = Unit
    override suspend fun delete(id: String) = Unit
    override fun observeTotalMonthlyEmi(): Flow<Double> = throw UnsupportedOperationException()
    override fun observeTotalPrincipalRemaining(): Flow<Double> =
        throw UnsupportedOperationException()
    override fun observeCount(): Flow<Int> = throw UnsupportedOperationException()
}

private object EmptyBillReminderRepository : BillReminderRepository {
    private val empty = MutableStateFlow<List<BillReminder>>(emptyList())
    override fun observeAll(): Flow<List<BillReminder>> = throw UnsupportedOperationException()
    override fun observeUpcoming(maxDays: Int): Flow<List<BillReminder>> = empty
    override fun observeById(id: String): Flow<BillReminder?> =
        throw UnsupportedOperationException()
    override suspend fun getById(id: String): BillReminder? = throw UnsupportedOperationException()
    override suspend fun upsert(bill: BillReminder) = throw UnsupportedOperationException()
    override suspend fun delete(id: String) = throw UnsupportedOperationException()
    override fun observeTotalUpcomingAmount(maxDays: Int): Flow<Double> =
        throw UnsupportedOperationException()
    override fun observeCount(): Flow<Int> = throw UnsupportedOperationException()
}

@OptIn(ExperimentalScreenDataStreamTestingApi::class)
private object LoadingEconomicRatesRepository : EconomicRatesRepository {
    override fun interestRateSeriesStream(
        key: InterestRateSeriesKey,
        scope: CoroutineScope,
    ): ScreenDataStream<InterestRateSeries> = screenDataStreamForTesting(
        state = MutableStateFlow(ScreenState.Loading),
        refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 64)
            .also { trigger -> trigger.onEach {}.launchIn(scope) },
    )

    override fun interestRateSeriesStream(
        keyFlow: Flow<InterestRateSeriesKey>,
        scope: CoroutineScope,
    ): ScreenDataStream<InterestRateSeries> = throw UnsupportedOperationException()
}

// endregion
