/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.fineract

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kpt.core.base.network.AuthScheme
import kpt.core.base.network.RuntimeHeaderStore
import kpt.core.base.network.setupDefaultHttpClient
import kpt.core.network.config.AppAccessPoints
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * The demo `fineract` access point (`app-profile/app.yaml`, `owner: demo`) as the template declares it:
 * its sandbox origin, its static tenant header and its derived Basic-auth header, end to end.
 *
 * These assertions pin demo VALUES, so they live in the demo-owned `fineract` package that
 * `remove-demo.sh` deletes together with the point. The value-free invariants that hold for every
 * fork's points stay in `config/` (`AuthSchemeTest`, `RuntimeHeaderTest`).
 */
class FineractPointTest {

    private val fineract = AppAccessPoints.points.first { it.id == "fineract" }

    @Test
    fun the_declared_scheme_drives_the_generated_header() {
        assertEquals(AuthScheme.BASIC, fineract.auth, "declared as auth: basic in app-profile")
        // The Authorization spec is DERIVED from `auth:` — nobody wrote that row by hand.
        val auth = fineract.headers.first { it.name == "Authorization" }
        assertEquals(AuthScheme.runtimeKeyFor("fineract"), auth.runtimeKey)
        assertNull(auth.value, "no credential may be baked into the binary")
    }

    @Test
    fun the_real_declared_point_sends_its_static_header_with_no_manual_step() = runTest {
        // The REAL generated point, not a synthetic spec list: this is what production builds from.
        val store = RuntimeHeaderStore()
        val seen = mutableListOf<Map<String, String?>>()

        val client = HttpClient(
            MockEngine { request ->
                seen += mapOf(
                    "tenant" to request.headers["Fineract-Platform-TenantId"],
                    "auth" to request.headers[HttpHeaders.Authorization],
                )
                respond("[]", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            },
        ) {
            setupDefaultHttpClient(
                baseUrl = fineract.effectiveUrl,
                // Exactly what ktorfitFor passes — nothing added by hand.
                dynamicHeaders = { store.resolve(fineract.headers) },
            )()
        }

        client.get("offices")
        // Declared `value:` in app.yaml -> on the wire, with no code touching it anywhere.
        assertEquals("default", seen[0]["tenant"])
        // …while the credential half stays absent until a token exists.
        assertNull(seen[0]["auth"])

        store[AuthScheme.runtimeKeyFor("fineract")] = "Basic dG9rZW4="
        client.get("offices")
        assertEquals("default", seen[1]["tenant"], "the static header is unaffected by sign-in")
        assertEquals("Basic dG9rZW4=", seen[1]["auth"])
    }

    @Test
    fun origin_and_path_join_into_one_effective_url() {
        assertEquals("https://sandbox.mifos.community/", fineract.baseUrl, "base_url is the ORIGIN only")
        assertEquals("fineract-provider/api/v1/", fineract.basePath)
        // The trailing slash is load-bearing: Ktor resolves a relative request path against this, so
        // without it `offices` would REPLACE the last segment and hit /api/offices.
        assertEquals(
            "https://sandbox.mifos.community/fineract-provider/api/v1/",
            fineract.effectiveUrl,
        )
    }

    @Test
    fun the_fineract_point_declares_both_header_kinds() {
        val byName = fineract.headers.associateBy { it.name }

        assertEquals("default", byName["Fineract-Platform-TenantId"]?.value, "tenant is a build-time constant")
        assertEquals("fineract.auth", byName[HttpHeaders.Authorization]?.runtimeKey, "auth is runtime-valued")
        assertNull(byName[HttpHeaders.Authorization]?.value, "no credential may be baked into the binary")
    }
}
