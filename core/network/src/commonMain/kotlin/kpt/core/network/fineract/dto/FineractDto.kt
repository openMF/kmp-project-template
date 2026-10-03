/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.fineract.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `POST /authentication` response — the Fineract sandbox's sign-in.
 *
 * The showcase for a RUNTIME header: nothing here is known at build time, and every subsequent call
 * needs the credential this returns.
 */
@Serializable
data class FineractAuthResponseDto(
    /** The authenticated username, echoed back. */
    val username: String? = null,
    /** The credential every subsequent call sends as its auth header — the runtime-header showcase. */
    @SerialName("base64EncodedAuthenticationKey")
    val base64EncodedAuthenticationKey: String? = null,
    /** Whether the sign-in succeeded. */
    val authenticated: Boolean = false,
    /** The user's office — the first authenticated field worth showing. */
    val officeName: String? = null,
)

/** `GET /offices` row — the smallest authenticated read that proves the header reached the server. */
@Serializable
data class FineractOfficeDto(
    /**
     * Office id. Nullable because every field on this endpoint is — the sandbox omits rather than nulls, and a strict
     * non-null would fail the whole parse.
     */
    val id: Long? = null,
    /** Office name, undecorated. Use [nameDecorated] when rendering a hierarchy. */
    val name: String? = null,
    /** Name with Fineract's hierarchy indentation, for a tree view. */
    @SerialName("nameDecorated") val nameDecorated: String? = null,
)
