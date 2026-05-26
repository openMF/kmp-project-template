/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.database.banking.converter

import kotlinx.datetime.LocalDate
import org.mifos.core.model.banking.BillCategory
import org.mifos.core.model.banking.LoanKind
import org.mifos.core.model.banking.Recurrence
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Round-trips every enum value and a handful of LocalDate edge cases.
 *
 * Crucial for catching enum renames (Room stores the `name`; renaming an enum
 * value without a migration would crash deserialization of existing rows).
 */
class BankingTypeConvertersTest {

    private val converters = BankingTypeConverters()

    @Test
    fun loanKindRoundTripCoversAllValues() {
        LoanKind.entries.forEach { kind ->
            val encoded = converters.fromLoanKind(kind)
            val decoded = converters.toLoanKind(encoded)
            assertEquals(kind, decoded)
        }
    }

    @Test
    fun recurrenceRoundTripCoversAllValues() {
        Recurrence.entries.forEach { value ->
            val encoded = converters.fromRecurrence(value)
            val decoded = converters.toRecurrence(encoded)
            assertEquals(value, decoded)
        }
    }

    @Test
    fun billCategoryRoundTripCoversAllValues() {
        BillCategory.entries.forEach { value ->
            val encoded = converters.fromBillCategory(value)
            val decoded = converters.toBillCategory(encoded)
            assertEquals(value, decoded)
        }
    }

    @Test
    fun localDateRoundTripIsoFormat() {
        val date = LocalDate(2026, 6, 1)
        val encoded = converters.fromLocalDate(date)
        assertEquals("2026-06-01", encoded)
        assertEquals(date, converters.toLocalDate(encoded))
    }

    @Test
    fun localDateLeapDayRoundTrip() {
        val date = LocalDate(2024, 2, 29)
        assertEquals(date, converters.toLocalDate(converters.fromLocalDate(date)))
    }

    @Test
    fun localDateYearEndRoundTrip() {
        val date = LocalDate(2099, 12, 31)
        assertEquals(date, converters.toLocalDate(converters.fromLocalDate(date)))
    }

    @Test
    fun unknownEnumNameThrows() {
        // Stored value drift from a removed/renamed enum entry: explicit failure
        // signals the migration writer they have a data-migration to do.
        assertFailsWith<IllegalArgumentException> {
            converters.toLoanKind("NOT_A_REAL_KIND")
        }
    }
}
