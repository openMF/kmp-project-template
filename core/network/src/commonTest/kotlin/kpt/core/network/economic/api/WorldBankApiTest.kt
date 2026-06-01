/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.economic.api

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kpt.core.model.economic.IndicatorKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WorldBankApiTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    @Test
    fun indicatorBuildsCorrectRequestUrl() = runTest {
        val capturedUrls = mutableListOf<String>()
        val api = buildWorldBankApi { request ->
            capturedUrls.add(request.url.toString())
            respondJson(WORLD_BANK_GDP_RESPONSE)
        }

        api.indicator(
            countryCode = "US",
            indicator = IndicatorKind.GDP.worldBankCode,
            dateRange = "2020:2024",
        )

        assertEquals(1, capturedUrls.size)
        val url = capturedUrls.single()
        assertTrue(url.contains("/v2/country/US/indicator/NY.GDP.MKTP.CD"), "url=$url")
        assertTrue(url.contains("format=json"), "url=$url")
        assertTrue(url.contains("date=2020%3A2024") || url.contains("date=2020:2024"), "url=$url")
    }

    @Test
    fun indicatorParsesTwoElementArrayPayload() = runTest {
        val api = buildWorldBankApi { respondJson(WORLD_BANK_GDP_RESPONSE) }

        val dto = api.indicator(
            countryCode = "US",
            indicator = IndicatorKind.GDP.worldBankCode,
            dateRange = "2020:2024",
        )

        assertEquals(5, dto.metadata?.total)
        assertEquals(5, dto.observations.size)
        // Wire order is reverse-chronological — our mapper sorts ascending in toDomain.
        assertEquals("2024", dto.observations.first().date)
    }

    @Test
    fun indicatorResultMapsToDomainSortedAscending() = runTest {
        val api = buildWorldBankApi { respondJson(WORLD_BANK_GDP_RESPONSE) }

        val domain = api.indicator(
            countryCode = "US",
            indicator = IndicatorKind.GDP.worldBankCode,
            dateRange = "2020:2024",
        ).toDomain(countryCode = "US", indicator = IndicatorKind.GDP)

        assertEquals("US", domain.countryCode)
        assertEquals("United States", domain.countryName)
        assertEquals(IndicatorKind.GDP, domain.indicator)
        assertEquals(listOf(2020, 2021, 2022, 2023, 2024), domain.observations.map { it.year })
    }

    private fun buildWorldBankApi(
        handler: suspend MockRequestHandleScope.(io.ktor.client.request.HttpRequestData) -> HttpResponseData,
    ): WorldBankApi {
        val mockEngine = MockEngine { request -> handler(request) }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }
        return Ktorfit.Builder()
            .baseUrl(WorldBankApi.BASE_URL)
            .httpClient(httpClient)
            .build()
            .createWorldBankApi()
    }

    private fun MockRequestHandleScope.respondJson(body: String): HttpResponseData =
        respond(
            content = ByteReadChannel(body),
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json"),
        )

    private companion object {
        const val WORLD_BANK_GDP_RESPONSE = """
        [
          {
            "page": 1,
            "pages": 1,
            "per_page": 50,
            "total": 5,
            "sourceid": "2",
            "lastupdated": "2026-04-30"
          },
          [
            {"indicator":{"id":"NY.GDP.MKTP.CD","value":"GDP (current US${'$'})"},"country":{"id":"US","value":"United States"},"countryiso3code":"USA","date":"2024","value":29183100000000.0},
            {"indicator":{"id":"NY.GDP.MKTP.CD","value":"GDP (current US${'$'})"},"country":{"id":"US","value":"United States"},"countryiso3code":"USA","date":"2023","value":27360935000000.0},
            {"indicator":{"id":"NY.GDP.MKTP.CD","value":"GDP (current US${'$'})"},"country":{"id":"US","value":"United States"},"countryiso3code":"USA","date":"2022","value":25744100000000.0},
            {"indicator":{"id":"NY.GDP.MKTP.CD","value":"GDP (current US${'$'})"},"country":{"id":"US","value":"United States"},"countryiso3code":"USA","date":"2021","value":23315080000000.0},
            {"indicator":{"id":"NY.GDP.MKTP.CD","value":"GDP (current US${'$'})"},"country":{"id":"US","value":"United States"},"countryiso3code":"USA","date":"2020","value":21354100000000.0}
          ]
        ]
        """
    }
}
