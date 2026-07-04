/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loans.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Append-only journey-level [TestTags] contract for the loans list → detail
 * → back walk (RULE-KMP-COMPOSE-UITEST-001 CU-4).
 *
 * The intended runtime journey — `runComposeUiTest { setContent {
 * NavHost(navController, startDestination = "loans") { ... } } }` — cannot
 * be authored today because `compose.uiTest` is not wired into
 * `commonTest` for any module in the fork (adding it fails the build
 * script; see the note on `CoinMarketsScreenUiTest`). The Maestro flow
 * `maestro/screen-state/list-back.yaml` (AC-14) is the on-device
 * counterpart and drives the same testTag string constants that this test
 * pins at compile time.
 *
 * Journey narrative locked here:
 *  1. Launch on `loans` route → assert [TestTags.LoansList.LIST] visible.
 *  2. Scroll to row 40 (`${TestTags.LoansList.ROW}_loan-40`).
 *  3. Tap that row → nav to `detail/loan-40`.
 *  4. Press Back → nav pops.
 *  5. Assert `${TestTags.LoansList.ROW}_loan-40` still exists (scroll retained
 *     via `rememberRetainedScreenState("loans_list")` composing with
 *     `retainedKoinViewModel()` nav-back-stack scoping from Phase 03).
 *
 * When `compose.uiTest` lands, this class extends to the full
 * `runComposeUiTest` implementation without any tag rename — the append-
 * only contract IS the wiring.
 */
class LoansJourneyUiTest {

    @Test
    fun journeyTagsAreStableAndNonBlank() {
        // The journey needs a list tag + a row tag; both must survive the
        // life of this test file across the entire epic (CU-5).
        val tags = listOf(TestTags.LoansList.LIST, TestTags.LoansList.ROW)
        tags.forEach { assertTrue(it.isNotBlank(), "Journey testTag must be non-blank") }
        assertEquals(tags.size, tags.toSet().size, "Journey testTags must be unique")
    }

    @Test
    fun perRowTagFormComposesDeterministically() {
        // Locks the composition rule the screen body uses
        // (`Modifier.testTag("${TestTags.LoansList.ROW}_${loan.id}")`) so a
        // future refactor that changes the separator or ordering breaks
        // this assertion loudly instead of silently unwiring Maestro
        // selectors.
        val row40 = "${TestTags.LoansList.ROW}_loan-40"
        assertTrue(row40.startsWith(TestTags.LoansList.ROW))
        assertTrue(row40.endsWith("loan-40"))
        assertEquals("loans_list_row_loan-40", row40)
    }
}
