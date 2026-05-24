/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.network.economic.dto

import kotlinx.serialization.json.Json
import org.mifos.core.model.economic.IndicatorKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorldBankResponseDtoTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    @Test
    fun parsesWorldBankTwoElementArrayResponse() {
        val payload = """
            [
              {
                "page": 1,
                "pages": 1,
                "per_page": 50,
                "total": 3,
                "sourceid": "2",
                "lastupdated": "2026-04-30"
              },
              [
                {
                  "indicator": {"id":"NY.GDP.MKTP.CD","value":"GDP (current US$)"},
                  "country": {"id":"US","value":"United States"},
                  "countryiso3code": "USA",
                  "date": "2022",
                  "value": 25462700000000.0,
                  "unit": "",
                  "obs_status": "",
                  "decimal": 0
                },
                {
                  "indicator": {"id":"NY.GDP.MKTP.CD","value":"GDP (current US$)"},
                  "country": {"id":"US","value":"United States"},
                  "countryiso3code": "USA",
                  "date": "2023",
                  "value": 27360935000000.0
                },
                {
                  "indicator": {"id":"NY.GDP.MKTP.CD","value":"GDP (current US$)"},
                  "country": {"id":"US","value":"United States"},
                  "countryiso3code": "USA",
                  "date": "2024",
                  "value": null
                }
              ]
            ]
        """.trimIndent()

        val dto = json.decodeFromString(WorldBankResponseDto.serializer(), payload)

        val metadata = dto.metadata
        assertNotNull(metadata)
        assertEquals(3, metadata.total)
        assertEquals(3, dto.observations.size)
        assertEquals("2024", dto.observations[2].date)
        assertNull(dto.observations[2].value)
    }

    @Test
    fun toDomainSortsObservationsAscendingByYear() {
        val payload = """
            [
              {"total": 3},
              [
                {"date":"2024","value":1.0, "country":{"id":"US","value":"United States"}},
                {"date":"2022","value":2.0, "country":{"id":"US","value":"United States"}},
                {"date":"2023","value":3.0, "country":{"id":"US","value":"United States"}}
              ]
            ]
        """.trimIndent()

        val dto = json.decodeFromString(WorldBankResponseDto.serializer(), payload)
        val domain = dto.toDomain(countryCode = "US", indicator = IndicatorKind.GDP)

        assertEquals(listOf(2022, 2023, 2024), domain.observations.map { it.year })
        assertEquals(listOf(2.0, 3.0, 1.0), domain.observations.map { it.value })
    }

    @Test
    fun toDomainPreservesNullValues() {
        val payload = """
            [
              {"total": 2},
              [
                {"date":"2022","value":null, "country":{"id":"US","value":"United States"}},
                {"date":"2023","value":4.5,  "country":{"id":"US","value":"United States"}}
              ]
            ]
        """.trimIndent()

        val dto = json.decodeFromString(WorldBankResponseDto.serializer(), payload)
        val domain = dto.toDomain(countryCode = "US", indicator = IndicatorKind.INFLATION_CPI)

        assertEquals(2, domain.observations.size)
        assertNull(domain.observations[0].value)
        assertEquals(4.5, domain.observations[1].value)
    }

    @Test
    fun toDomainPopulatesCountryNameFromFirstObservation() {
        val payload = """
            [
              {"total": 1},
              [
                {"date":"2023","value":4.5, "country":{"id":"IN","value":"India"}}
              ]
            ]
        """.trimIndent()

        val dto = json.decodeFromString(WorldBankResponseDto.serializer(), payload)
        val domain = dto.toDomain(countryCode = "IN", indicator = IndicatorKind.GDP)

        assertEquals("India", domain.countryName)
        assertEquals("IN", domain.countryCode)
        assertEquals(IndicatorKind.GDP, domain.indicator)
        assertEquals("World Bank Open Data", domain.source)
    }

    @Test
    fun parsesObjectShapedErrorResponseAsEmpty() {
        // World Bank returns a single object (not array) on some error conditions:
        // { "message": [...] }
        val payload = """
            {
              "message": [
                {"id": "120", "key": "Invalid value", "value": "The provided parameter value is not valid"}
              ]
            }
        """.trimIndent()

        val dto = json.decodeFromString(WorldBankResponseDto.serializer(), payload)
        assertNull(dto.metadata)
        assertTrue(dto.observations.isEmpty())
    }

    @Test
    fun parsesEmptyObservationsArrayWhenNoData() {
        val payload = """
            [
              {"page": 1, "pages": 0, "per_page": 50, "total": 0},
              []
            ]
        """.trimIndent()

        val dto = json.decodeFromString(WorldBankResponseDto.serializer(), payload)
        assertEquals(0, dto.metadata?.total)
        assertTrue(dto.observations.isEmpty())

        val domain = dto.toDomain(countryCode = "XX", indicator = IndicatorKind.GINI)
        assertTrue(domain.observations.isEmpty())
        assertEquals("XX", domain.countryName) // falls back to country code when no data
    }
}
