/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.fineract.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import kpt.core.base.network.annotation.ApiBinding
import kpt.core.network.fineract.dto.FineractAuthResponseDto
import kpt.core.network.fineract.dto.FineractOfficeDto

/**
 * Mifos Fineract sandbox — the showcase for DECLARED, RUNTIME-VALUED headers.
 *
 * Every call carries two headers, and neither is written here: both are declared on the `fineract`
 * access point in `app-profile/app.yaml#network.access_points[].headers[]`.
 *
 *   `Fineract-Platform-TenantId: default`     static — known at build time, baked in.
 *   `Authorization: Basic …`                  runtime — does not exist until [authenticate] returns.
 *
 * That split is the point. The endpoint states WHICH headers it needs; login supplies only the
 * VALUE, by writing it to `RuntimeHeaderStore["fineract.auth"]`. Because the store is read inside
 * `defaultRequest`, the singleton client built at Koin start — long before anyone signs in — picks
 * the credential up on the very next call, with no client rebuild and no bypassing of the generated
 * `@ApiBinding` wiring.
 *
 * API reference: https://sandbox.mifos.community/fineract-provider/swagger-ui/index.html
 */
@ApiBinding("fineract")
interface FineractApi {

    /**
     * Sign in. The `Authorization` header is NOT required for this call — it is what produces it:
     * the returned `base64EncodedAuthenticationKey` becomes `Basic <key>` in the runtime store.
     */
    @POST("authentication")
    suspend fun authenticate(@Body request: Map<String, String>): FineractAuthResponseDto

    /**
     * The smallest authenticated read. Useful as a proof that the runtime header reached the server:
     * it answers 401 before sign-in and 200 after, with no client rebuild in between.
     */
    @GET("offices")
    suspend fun offices(): List<FineractOfficeDto>
}
