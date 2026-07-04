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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Append-only [TestTags] contract for `HomeScreen` (the bottom-nav
 * dashboard) — RULE-KMP-COMPOSE-UITEST-001 CU-5.
 *
 * The full `@OptIn(ExperimentalTestApi) runComposeUiTest { setContent { ... } }`
 * render assertion (dashboard renders, per-tile combined-state emission
 * survives recomposition, tab-switch preserves scroll) is deferred until
 * `compose.uiTest` is wired into `commonTest` across all six KMP targets —
 * no module in this fork does that yet (see the note on
 * `feature/crypto/.../CoinMarketsScreenUiTest.kt`).
 *
 * Until then this test locks the tag contract at compile time: every
 * constant referenced here must exist on the shipped [TestTags.Home]
 * object, so renaming or removing one breaks the build (CU-5 append-only).
 * Once `compose.uiTest` lands, the deferred render assertion is a
 * mechanical extension — the same tag names drive `onNodeWithTag(...)`.
 */
class HomeScreenUiTest {

    @Test
    fun testTagsAreStableAndNonBlank() {
        val tags = listOf(
            TestTags.Home.DASHBOARD_SCROLL,
            TestTags.Home.TILE_LOANS,
            TestTags.Home.TILE_BILLS,
            TestTags.Home.TILE_RATES,
            TestTags.Home.TILE_FX,
        )
        tags.forEach { assertTrue(it.isNotBlank(), "TestTag must be non-blank") }
        assertEquals(tags.size, tags.toSet().size, "TestTags must be unique")
    }

    @Test
    fun dashboardScrollAndTilesDivergeInPrefix() {
        // Guards against a Maestro selector accidentally matching a tile row
        // when it means to grab the scroll container.
        assertTrue(TestTags.Home.DASHBOARD_SCROLL.contains("scroll"))
        assertTrue(TestTags.Home.TILE_LOANS.startsWith("home_tile"))
        assertTrue(TestTags.Home.TILE_BILLS.startsWith("home_tile"))
        assertTrue(TestTags.Home.TILE_RATES.startsWith("home_tile"))
        assertTrue(TestTags.Home.TILE_FX.startsWith("home_tile"))
    }
}
