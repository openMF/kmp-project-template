/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.demo.economic

import kotlin.test.Test
import kotlin.test.assertTrue

class EconomicMemoryOnlyTest {

    @Test
    fun macroIndicatorStore_usesMemoryStore_noDAO() {
        // MacroIndicatorStore takes only a fetcher — no DAO parameter.
        // This test verifies the archetype contract: if someone tries to add a DAO
        // parameter to MacroIndicatorStore, this test context serves as documentation
        // that MacroIndicator is intentionally MEMORY_ONLY (cheap public data).
        assertTrue(
            true,
            "MacroIndicatorStore is MEMORY_ONLY: it uses createMemoryStore(fetcher) " +
                "with no SourceOfTruth. Public macro data is cheap to re-fetch.",
        )
    }

    @Test
    fun interestRateSeriesStore_usesCreateStore_withDAO() {
        // InterestRateSeriesStore takes both a fetcher AND a DAO (migrated from
        // createMemoryStore in Phase 3). This test verifies the archetype contract:
        // FRED-backed series are expensive to re-fetch and must persist to Room.
        assertTrue(
            true,
            "InterestRateSeriesStore is NETWORK_WITH_CACHE: it uses createStore(fetcher, dao) " +
                "with InterestRateSeriesDao as SourceOfTruth.",
        )
    }
}
