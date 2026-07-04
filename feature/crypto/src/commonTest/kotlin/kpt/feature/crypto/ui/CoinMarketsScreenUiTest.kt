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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Append-only [TestTags] contract for [CoinMarketsScreen] (RULE-KMP-COMPOSE-UITEST-001 CU-5).
 *
 * The full `runComposeUiTest` render assertion is deferred until `compose.uiTest`
 * is wired into `commonTest` across all six KMP targets — no module in the fork
 * does this yet (see build.gradle.kts note). Until then this test locks the tag
 * contract at compile time: every constant referenced here must exist on the
 * Screen's [TestTags] object, so renaming or removing one breaks the build.
 */
class CoinMarketsScreenUiTest {

    @Test
    fun testTagsAreStableAndNonBlank() {
        // Referencing each constant makes a rename/removal a compile error (CU-5).
        val tags = listOf(TestTags.LIST, TestTags.ROW, TestTags.LOADING)
        tags.forEach { assertTrue(it.isNotBlank(), "TestTag must be non-blank") }
        assertEquals(tags.size, tags.toSet().size, "TestTags must be unique")
    }
}
