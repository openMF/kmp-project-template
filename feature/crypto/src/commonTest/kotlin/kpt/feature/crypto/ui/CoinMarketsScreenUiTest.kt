/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.crypto.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import kpt.core.designsystem.theme.KptTheme
import kotlin.test.Test

/**
 * Compose Multiplatform UI test for [CoinMarketsScreen].
 *
 * Renders the screen with a fake-backed [CoinMarketsViewModel] (no Koin, no network,
 * no Room) wrapped in [KptTheme], and asserts the root [Scaffold] testTag
 * [TestTags.SCREEN] — always rendered regardless of paging state.
 *
 * [TestTags.LIST] and [TestTags.ROW] are on [PagingScreenContent]'s
 * `LazyColumn`, which only appears in [ScreenState.Content]. Since the fake
 * returns an empty fetcher the stream ends in [ScreenState.Empty]; the [Scaffold]
 * root tag is the only stable, always-visible assertion target
 * (RULE-KMP-COMPOSE-UITEST-001 CU-1..CU-3).
 */
@OptIn(ExperimentalTestApi::class)
class CoinMarketsScreenUiTest {

    @Test
    fun screenScaffoldIsDisplayed() = runComposeUiTest {
        val viewModel = CoinMarketsViewModel(FakeCryptoRepository())
        setContent {
            KptTheme {
                CoinMarketsScreen(
                    onBackClick = {},
                    onCoinClick = {},
                    viewModel = viewModel,
                )
            }
        }
        onNodeWithTag(TestTags.SCREEN).assertIsDisplayed()
    }
}
