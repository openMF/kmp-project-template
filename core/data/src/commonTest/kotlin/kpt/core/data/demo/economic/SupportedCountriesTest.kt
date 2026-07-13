/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.demo.economic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Locks the curated supported-countries list:
 * - Contains the major economies the toolkit ships with by default
 * - Every code is a valid 2-letter ISO 3166-1 alpha-2 (uppercase A-Z)
 * - Codes are unique
 * - The default country (`"US"`) is present
 * - Lookup helpers behave correctly
 * - Search is case-insensitive and prefix/substring-aware over both code and name
 */
class SupportedCountriesTest {

    @Test
    fun listHasAtLeastTwentyEntries() {
        // Sub-plan target is ~30; require ≥20 so a future trim doesn't silently
        // strip the catalogue. Cap at 250 (there are 195 sovereign nations) to
        // protect against accidental whole-world inserts in a curated list.
        assertTrue(
            SupportedCountries.list.size in 20..250,
            "Expected ~30 curated countries, got ${SupportedCountries.list.size}",
        )
    }

    @Test
    fun everyCodeIsTwoUppercaseLetters() {
        SupportedCountries.list.forEach { country ->
            assertEquals(2, country.code.length, "Code ${country.code} should be 2 chars")
            assertTrue(
                country.code.all { it in 'A'..'Z' },
                "Code ${country.code} should be uppercase A-Z",
            )
        }
    }

    @Test
    fun codesAreUnique() {
        val codes = SupportedCountries.list.map { it.code }
        assertEquals(
            codes.size,
            codes.toSet().size,
            "Duplicate ISO codes in supported list: " +
                codes.groupingBy { it }.eachCount().filterValues { it > 1 },
        )
    }

    @Test
    fun defaultCountryUsIsPresent() {
        assertNotNull(SupportedCountries.findByCode("US"))
    }

    @Test
    fun everyEntryHasNonBlankNameAndFlag() {
        SupportedCountries.list.forEach { c ->
            assertTrue(c.name.isNotBlank(), "Country ${c.code} has blank name")
            assertTrue(c.flagEmoji.isNotBlank(), "Country ${c.code} has blank flag")
        }
    }

    @Test
    fun findByCodeIsCaseInsensitive() {
        val upper = SupportedCountries.findByCode("US")
        val lower = SupportedCountries.findByCode("us")
        val mixed = SupportedCountries.findByCode("Us")
        assertNotNull(upper)
        assertEquals(upper, lower)
        assertEquals(upper, mixed)
    }

    @Test
    fun findByCodeReturnsNullForUnknown() {
        assertNull(SupportedCountries.findByCode("ZZ"))
    }

    @Test
    fun searchEmptyQueryReturnsFullList() {
        assertEquals(SupportedCountries.list, SupportedCountries.search(""))
        assertEquals(SupportedCountries.list, SupportedCountries.search("   "))
    }

    @Test
    fun searchMatchesByCodePrefixCaseInsensitively() {
        val match = SupportedCountries.search("us")
        assertTrue(match.any { it.code == "US" }, "Expected US in results for 'us'")
    }

    @Test
    fun searchMatchesByNameSubstringCaseInsensitively() {
        val match = SupportedCountries.search("ind")
        assertTrue(
            match.any { it.code == "IN" },
            "Expected India (IN) in results for 'ind', got ${match.map { it.code }}",
        )
    }

    @Test
    fun searchUnknownReturnsEmpty() {
        assertTrue(SupportedCountries.search("zzzqqqxxx").isEmpty())
    }
}
