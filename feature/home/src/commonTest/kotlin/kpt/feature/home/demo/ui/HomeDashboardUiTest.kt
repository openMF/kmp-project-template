/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.home.demo.ui

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.economic.EconomicRatesRepository
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.economic.FredApiKeyMissingException
import kpt.core.model.economic.InterestRateSeries
import kpt.core.store.economic.impl.InterestRateSeriesKey
import kpt.feature.home.demo.HomeDashboard
import kpt.feature.home.ui.TestTags
import kotlin.test.Test

/**
 * Compose Multiplatform UI test for the multi-source [HomeDashboard] — the "hard case"
 * (a 4-repository dashboard `internal` composable) proving the commonTest harness scales
 * beyond a single-repo screen. Reuses the fake repositories already defined in
 * [HomeDashboardViewModelTest] (widened to `internal`) to build the real [HomeViewModel]
 * with no Koin, wraps in [KptTheme], and asserts the dashboard scroll container renders.
 */
@OptIn(ExperimentalTestApi::class)
class HomeDashboardUiTest {

    @Test
    fun dashboardScrollIsDisplayed() = runComposeUiTest {
        setDashboard(LoadingEconomicRatesRepository)
        onNodeWithTag(TestTags.Home.DASHBOARD_SCROLL).assertIsDisplayed()
    }

    @Test
    fun missingFredKeyShowsSetupNoticeOnRatesCard() = runComposeUiTest {
        setDashboard(FredKeyMissingEconomicRatesRepository)
        onNodeWithTag(TestTags.Home.RATES_FRED_KEY_MISSING).assertExists()
    }

    private fun ComposeUiTest.setDashboard(rates: EconomicRatesRepository) {
        val viewModel = HomeViewModel(
            loanRepository = EmptyLoanRepository,
            billReminderRepository = EmptyBillReminderRepository,
            economicRatesRepository = rates,
            currencyRepository = FakeDashboardCurrencyRepository(),
        )
        setContent {
            KptTheme {
                HomeDashboard(
                    onNavigateToLoans = {},
                    onNavigateToBills = {},
                    onNavigateToRates = {},
                    onNavigateToExchangeRates = {},
                    onNavigateToRateHistory = {},
                    onNavigateToMacro = {},
                    onNavigateToEmi = {},
                    onNavigateToAffordability = {},
                    onNavigateToAmortization = {},
                    onNavigateToLoanComparison = {},
                    onNavigateToLoanCalcWizard = {},
                    onNavigateToCrypto = {},
                    viewModel = viewModel,
                )
            }
        }
    }
}

/** Every FRED series fails the way a build without a key does. */
@OptIn(ExperimentalScreenDataStreamTestingApi::class)
private object FredKeyMissingEconomicRatesRepository : EconomicRatesRepository {
    override fun interestRateSeriesStream(
        key: InterestRateSeriesKey,
        scope: CoroutineScope,
    ): ScreenDataStream<InterestRateSeries> = screenDataStreamForTesting(
        state = MutableStateFlow(ScreenState.Error(FredApiKeyMissingException())),
        refreshTrigger = MutableSharedFlow(extraBufferCapacity = 64),
    )

    override fun interestRateSeriesStream(
        keyFlow: Flow<InterestRateSeriesKey>,
        scope: CoroutineScope,
    ): ScreenDataStream<InterestRateSeries> = throw UnsupportedOperationException()
}
