/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.config

import kpt.core.base.network.UrlType

/** Transport kind of a declared network access point. */
enum class AccessPointKind {
    /** A REST endpoint reached over Ktor/Ktorfit. */
    REST,

    /** A Supabase project reached over supabase-kt (Postgrest by default). */
    SUPABASE,
}

/**
 * One declared network access point — a named endpoint the app talks to.
 *
 * @param type the [UrlType] key clients thread through `setupDefaultHttpClient` (REST) or the
 *   logical name of the Supabase data plane.
 * @param kind [AccessPointKind.REST] (Ktor/Ktorfit) or [AccessPointKind.SUPABASE] (supabase-kt).
 * @param baseUrl the resolved base URL (REST) or the Supabase project URL.
 * @param loggableHost the host enabled for HTTP logging on this access point.
 */
data class AccessPoint(
    val type: UrlType,
    val kind: AccessPointKind,
    val baseUrl: String,
    val loggableHost: String,
)

/**
 * The single declarative registry of every network access point this app exposes — REST and
 * Supabase in ONE place.
 *
 * [AppMultiUrlConfigProvider] resolves each REST base URL from here, and the Supabase DI binding
 * reads the [supabasePoint] project URL from here, so there is exactly one source of truth for
 * "which servers does this app talk to". A fork edits ONLY this object to declare its N endpoints —
 * no per-client wiring changes are needed.
 *
 * The default entries below are real `https://` URLs (no `YOUR_` placeholder) so the Supabase point
 * is default-configured, not inert. A fork replaces these hosts with its own.
 */
object AccessPointRegistry {
    /** Every declared access point — REST and Supabase — for this app. */
    val points: List<AccessPoint> = listOf(
        AccessPoint(
            type = AppUrlTypes.MAIN,
            kind = AccessPointKind.REST,
            baseUrl = "https://api.example.com/",
            loggableHost = "api.example.com",
        ),
        AccessPoint(
            type = AppUrlTypes.STAGING,
            kind = AccessPointKind.REST,
            baseUrl = "https://staging.example.com/",
            loggableHost = "staging.example.com",
        ),
        AccessPoint(
            type = AppUrlTypes.SUPABASE_DATA,
            kind = AccessPointKind.SUPABASE,
            baseUrl = "https://project.supabase.co",
            loggableHost = "project.supabase.co",
        ),
    )

    /** Resolve the REST base URL registered for [type], or `null` if none is declared. */
    fun restBaseUrl(type: UrlType): String? =
        points.firstOrNull { it.type == type && it.kind == AccessPointKind.REST }?.baseUrl

    /** The first Supabase access point, or `null` if none is declared. */
    fun supabasePoint(): AccessPoint? =
        points.firstOrNull { it.kind == AccessPointKind.SUPABASE }

    /** Every declared host enabled for HTTP logging, across all access points. */
    fun loggableHosts(): List<String> = points.map { it.loggableHost }
}
