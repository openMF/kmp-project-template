/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.economic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [IndicatorKind] — the World Bank indicator-code mapping is the
 * machine-readable contract between the toolkit's screens and the World Bank
 * Open Data API. Any change here cascades into URL paths in [WorldBankApi].
 */
class IndicatorKindTest {

    @Test
    fun gdpMapsToWorldBankCode() {
        assertEquals("NY.GDP.MKTP.CD", IndicatorKind.GDP.worldBankCode)
    }

    @Test
    fun inflationMapsToWorldBankCode() {
        assertEquals("FP.CPI.TOTL.ZG", IndicatorKind.INFLATION_CPI.worldBankCode)
    }

    @Test
    fun unemploymentMapsToWorldBankCode() {
        assertEquals("SL.UEM.TOTL.ZS", IndicatorKind.UNEMPLOYMENT.worldBankCode)
    }

    @Test
    fun gdpPerCapitaMapsToWorldBankCode() {
        assertEquals("NY.GDP.PCAP.CD", IndicatorKind.GDP_PER_CAPITA.worldBankCode)
    }

    @Test
    fun giniMapsToWorldBankCode() {
        assertEquals("SI.POV.GINI", IndicatorKind.GINI.worldBankCode)
    }

    @Test
    fun fromWorldBankCodeRoundTripsForAllKinds() {
        for (kind in IndicatorKind.entries) {
            assertEquals(kind, IndicatorKind.fromWorldBankCode(kind.worldBankCode))
        }
    }

    @Test
    fun fromWorldBankCodeReturnsNullForUnknownCode() {
        assertNull(IndicatorKind.fromWorldBankCode("FAKE.UNKNOWN.CODE"))
    }

    @Test
    fun fromWorldBankCodeReturnsNullForEmptyString() {
        assertNull(IndicatorKind.fromWorldBankCode(""))
    }
}
