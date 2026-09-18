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
    val username: String? = null,
    @SerialName("base64EncodedAuthenticationKey")
    val base64EncodedAuthenticationKey: String? = null,
    val authenticated: Boolean = false,
    val officeName: String? = null,
)

/** `GET /offices` row — the smallest authenticated read that proves the header reached the server. */
@Serializable
data class FineractOfficeDto(
    val id: Long? = null,
    val name: String? = null,
    @SerialName("nameDecorated") val nameDecorated: String? = null,
)
