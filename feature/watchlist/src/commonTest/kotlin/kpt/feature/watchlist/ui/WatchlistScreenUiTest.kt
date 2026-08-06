/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.watchlist.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kpt.feature.watchlist.testing.FakeWatchlistRepository
import kpt.feature.watchlist.testing.item
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Compose Multiplatform UI test for [WatchlistScreen] (`read_local_list` demo). Renders with a
 * fake-backed [WatchlistViewModel] (no Koin, no Room), asserts the always-present Scaffold root
 * and a content row, and proves the row's remove IconButton is wired to the local write.
 * (RULE-KMP-COMPOSE-UITEST-001 CU-1..CU-3.)
 */
@OptIn(ExperimentalTestApi::class)
class WatchlistScreenUiTest {

    @Test
    fun screenScaffoldIsDisplayed() = runComposeUiTest {
        val vm = WatchlistViewModel(FakeWatchlistRepository(listOf(item("btc"))))
        setContent {
            MaterialTheme {
                WatchlistScreen(onBackClick = {}, viewModel = vm)
            }
        }
        onNodeWithTag(TestTags.Watchlist.SCREEN).assertIsDisplayed()
    }

    @Test
    fun removeIconButton_dispatchesRemove() = runComposeUiTest {
        val repo = FakeWatchlistRepository(listOf(item("btc")))
        val vm = WatchlistViewModel(repo)
        setContent {
            MaterialTheme {
                WatchlistScreen(onBackClick = {}, viewModel = vm)
            }
        }
        waitUntil { onAllNodesWithTag(TestTags.Watchlist.REMOVE_PREFIX + "btc").fetchSemanticsNodes().isNotEmpty() }
        onNodeWithTag(TestTags.Watchlist.REMOVE_PREFIX + "btc").performClick()
        waitForIdle()
        assertEquals(listOf("btc"), repo.removeCalls)
    }
}
