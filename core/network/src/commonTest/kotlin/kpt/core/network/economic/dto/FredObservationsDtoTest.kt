/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.economic.dto

import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FredObservationsDtoTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    @Test
    fun parsesValidFredJsonPayload() {
        val payload = """
            {
              "realtime_start": "2026-05-23",
              "realtime_end": "2026-05-23",
              "observation_start": "2026-05-20",
              "observation_end": "2026-05-23",
              "units": "lin",
              "output_type": 1,
              "file_type": "json",
              "order_by": "observation_date",
              "sort_order": "asc",
              "count": 3,
              "offset": 0,
              "limit": 100000,
              "observations": [
                {"realtime_start":"2026-05-23","realtime_end":"2026-05-23","date":"2026-05-21","value":"4.33"},
                {"realtime_start":"2026-05-23","realtime_end":"2026-05-23","date":"2026-05-22","value":"4.33"},
                {"realtime_start":"2026-05-23","realtime_end":"2026-05-23","date":"2026-05-23","value":"4.34"}
              ]
            }
        """.trimIndent()

        val dto = json.decodeFromString(FredObservationsDto.serializer(), payload)
        assertEquals(3, dto.count)
        assertEquals(3, dto.observations.size)
        assertEquals("4.33", dto.observations.first().value)
    }

    @Test
    fun toDomainStripsDotNoDataMarker() {
        val dto = FredObservationsDto(
            observations = listOf(
                FredObservationDto(date = "2026-05-20", value = "4.33"),
                FredObservationDto(date = "2026-05-21", value = "."),
                FredObservationDto(date = "2026-05-22", value = "4.35"),
            ),
        )

        val domain = dto.toDomain(seriesId = "DFF", name = "Federal Funds Rate", unit = "%")

        assertEquals("DFF", domain.seriesId)
        assertEquals("Federal Funds Rate", domain.name)
        assertEquals("%", domain.unit)
        assertEquals("FRED", domain.source)
        assertEquals(2, domain.observations.size)
        assertEquals(LocalDate(2026, 5, 20), domain.observations[0].date)
        assertEquals(4.33, domain.observations[0].value)
        assertEquals(4.35, domain.observations[1].value)
    }

    @Test
    fun toDomainSetsCurrentFromLastObservation() {
        val dto = FredObservationsDto(
            observations = listOf(
                FredObservationDto(date = "2026-05-20", value = "4.33"),
                FredObservationDto(date = "2026-05-21", value = "4.35"),
                FredObservationDto(date = "2026-05-22", value = "4.50"),
            ),
        )

        val domain = dto.toDomain(seriesId = "DFF", name = "Federal Funds Rate", unit = "%")
        assertEquals(4.50, domain.current)
    }

    @Test
    fun toDomainHandlesEmptyObservationsList() {
        val dto = FredObservationsDto(observations = emptyList())
        val domain = dto.toDomain(seriesId = "DFF", name = "Federal Funds Rate", unit = "%")

        assertTrue(domain.observations.isEmpty())
        assertEquals(0.0, domain.current)
        assertEquals("DFF", domain.seriesId)
    }

    @Test
    fun toDomainOrNullRejectsDotMarker() {
        val row = FredObservationDto(date = "2026-05-23", value = ".")
        assertNull(row.toDomainOrNull())
    }

    @Test
    fun toDomainOrNullRejectsNonNumericValue() {
        val row = FredObservationDto(date = "2026-05-23", value = "not a number")
        assertNull(row.toDomainOrNull())
    }

    @Test
    fun toDomainOrNullRejectsInvalidDate() {
        val row = FredObservationDto(date = "not a date", value = "4.33")
        assertNull(row.toDomainOrNull())
    }

    @Test
    fun toDomainOrNullParsesValidRow() {
        val row = FredObservationDto(date = "2026-05-23", value = "4.34")
        val parsed = row.toDomainOrNull()
        assertEquals(LocalDate(2026, 5, 23), parsed?.date)
        assertEquals(4.34, parsed?.value)
    }

    @Test
    fun fredDtoTolaratesMissingOptionalFields() {
        val payload = """
            {
              "observations": [
                {"date":"2026-05-23","value":"4.34"}
              ]
            }
        """.trimIndent()

        val dto = json.decodeFromString(FredObservationsDto.serializer(), payload)
        assertEquals(1, dto.observations.size)
        assertNull(dto.units)
        assertNull(dto.count)
    }
}
