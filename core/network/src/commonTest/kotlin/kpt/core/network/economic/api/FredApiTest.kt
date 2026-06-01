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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FredApiTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    @Test
    fun seriesObservationsBuildsCorrectRequestUrl() = runTest {
        val capturedUrls = mutableListOf<String>()
        val api = buildFredApi { request ->
            capturedUrls.add(request.url.toString())
            respondJson(FRED_SAMPLE_RESPONSE)
        }

        api.seriesObservations(
            seriesId = "DFF",
            apiKey = "test-key",
            observationStart = "2026-05-20",
            observationEnd = "2026-05-23",
        )

        assertEquals(1, capturedUrls.size)
        val url = capturedUrls.single()
        assertEquals(true, url.contains("/fred/series/observations"))
        assertEquals(true, url.contains("series_id=DFF"))
        assertEquals(true, url.contains("api_key=test-key"))
        assertEquals(true, url.contains("file_type=json"))
        assertEquals(true, url.contains("observation_start=2026-05-20"))
        assertEquals(true, url.contains("observation_end=2026-05-23"))
    }

    @Test
    fun seriesObservationsParsesResponsePayloadIntoDto() = runTest {
        val api = buildFredApi { respondJson(FRED_SAMPLE_RESPONSE) }

        val dto = api.seriesObservations(
            seriesId = "DFF",
            apiKey = "test-key",
            observationStart = "2026-05-20",
            observationEnd = "2026-05-23",
        )

        assertEquals(3, dto.count)
        assertEquals(3, dto.observations.size)
        assertEquals("2026-05-21", dto.observations[0].date)
        assertEquals("4.33", dto.observations[0].value)
    }

    @Test
    fun seriesObservationsResultMapsToDomainCorrectly() = runTest {
        val api = buildFredApi { respondJson(FRED_SAMPLE_RESPONSE) }

        val domain = api.seriesObservations(
            seriesId = "DFF",
            apiKey = "test-key",
            observationStart = "2026-05-20",
            observationEnd = "2026-05-23",
        ).toDomain(
            seriesId = "DFF",
            name = "Federal Funds Rate",
            unit = "%",
        )

        assertEquals("DFF", domain.seriesId)
        assertEquals("Federal Funds Rate", domain.name)
        assertEquals("%", domain.unit)
        assertNotNull(domain.observations.firstOrNull())
        assertEquals(3, domain.observations.size)
    }

    private fun buildFredApi(
        handler: suspend MockRequestHandleScope.(io.ktor.client.request.HttpRequestData) -> HttpResponseData,
    ): FredApi {
        val mockEngine = MockEngine { request -> handler(request) }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }
        return Ktorfit.Builder()
            .baseUrl(FredApi.BASE_URL)
            .httpClient(httpClient)
            .build()
            .createFredApi()
    }

    private fun MockRequestHandleScope.respondJson(body: String): HttpResponseData =
        respond(
            content = ByteReadChannel(body),
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json"),
        )

    private companion object {
        const val FRED_SAMPLE_RESPONSE = """
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
        """
    }
}
