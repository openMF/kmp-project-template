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
 * Append-only [TestTags] contract for [PersonalLoansListScreen]
 * (RULE-KMP-COMPOSE-UITEST-001 CU-5).
 *
 * The full `@OptIn(ExperimentalTestApi) runComposeUiTest { setContent { ... } }`
 * render assertion (list renders, scroll-restore-across-recomposition,
 * per-row testTag lookup) is deferred until `compose.uiTest` is wired into
 * `commonTest` across all six KMP targets — no module in this fork does
 * that yet (adding `implementation(compose.uiTest)` to
 * `feature/loans/build.gradle.kts` fails the build script because the
 * accessor was deprecated-as-error in the Compose Multiplatform version the
 * fork pins).
 *
 * Until then this test locks the tag contract at compile time: every
 * constant referenced here must exist on the shipped [TestTags.LoansList]
 * object, so renaming or removing one breaks the build (CU-5 append-only).
 * Once `compose.uiTest` lands, the deferred render assertion is a mechanical
 * extension of this class — the same tag names drive the
 * `onNodeWithTag(...)` selectors, no re-authoring of the contract required.
 *
 * Mirrors the shape shipped by
 * `feature/crypto/.../ui/CoinMarketsScreenUiTest.kt` (Phase 04).
 */
class PersonalLoansListScreenUiTest {

    @Test
    fun testTagsAreStableAndNonBlank() {
        // Referencing each constant makes a rename/removal a compile error (CU-5).
        val tags = listOf(
            TestTags.LoansList.LIST,
            TestTags.LoansList.ROW,
            TestTags.LoansList.FAB,
            TestTags.LoansList.DELETE_CONFIRM,
        )
        tags.forEach { assertTrue(it.isNotBlank(), "TestTag must be non-blank") }
        assertEquals(tags.size, tags.toSet().size, "TestTags must be unique")
    }

    @Test
    fun listAndRowTagsShareCommonPrefix() {
        // The per-row tag is composed as `"${ROW}_${loan.id}"` in the screen
        // body — the two prefixes must diverge so a row lookup doesn't
        // accidentally match the LazyColumn container.
        assertTrue(TestTags.LoansList.LIST != TestTags.LoansList.ROW)
        assertTrue(TestTags.LoansList.ROW.startsWith("loans_list"))
    }
}
