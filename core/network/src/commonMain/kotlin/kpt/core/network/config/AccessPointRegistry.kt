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
    /** Stable string id a fork references from `restApi<T>(id)` / `supabaseClientFor(id)`. */
    val id: String,
    val kind: AccessPointKind,
    val baseUrl: String,
    val loggableHost: String,
    /** UrlType key for dynamic base-URL switching ([kpt.core.base.network.MultiUrlConfigProvider]).
     *  Defaults to the id upper-cased, so a fixed-URL API doesn't need to declare one. */
    val type: UrlType = UrlType(id.uppercase()),
)

/**
 * The single declarative registry of every network access point this app exposes — REST and
 * Supabase in ONE place.
 *
 * [AppMultiUrlConfigProvider] resolves each REST base URL from here, the Supabase DI binding reads the
 * [supabasePoint] project URL from here, and the `restApi("<id>")` DSL resolves transports by [byId] —
 * so there is exactly one source of truth for "which servers does this app talk to".
 *
 * **SoT: `app-profile/app.yaml#network.access_points`.** A fork declares its N typed endpoints (REST +
 * Supabase) there and runs `./gradlew syncForkConfig` — which regenerates the sentinel-bounded [points]
 * block below from app-profile (do not hand-edit that block). core/network then auto-builds a transport
 * per access point, so a fork only writes API interfaces + one `restApi("<id>")` line each.
 *
 * The default entries are real `https://` URLs (no `YOUR_` placeholder) so the Supabase point is
 * default-configured, not inert. A fork replaces these with its own.
 */
object AccessPointRegistry {
    /** Every declared access point — REST and Supabase — for this app. */
    // syncForkConfig:access-points:begin — GENERATED from app-profile/app.yaml#network.access_points.
    // Edit access points THERE (the SoT) and run `./gradlew syncForkConfig`; do not hand-edit this block.
    // `type` defaults to UrlType(id.uppercase()) — value-class-equal to the AppUrlTypes.* constants.
    val points: List<AccessPoint> = listOf(
        AccessPoint(id = "main", kind = AccessPointKind.REST, baseUrl = "https://api.example.com/", loggableHost = "api.example.com"),
        AccessPoint(id = "staging", kind = AccessPointKind.REST, baseUrl = "https://staging.example.com/", loggableHost = "staging.example.com"),
        AccessPoint(id = "supabase_data", kind = AccessPointKind.SUPABASE, baseUrl = "https://project.supabase.co", loggableHost = "project.supabase.co"),
        AccessPoint(id = "jsonplaceholder", kind = AccessPointKind.REST, baseUrl = "https://jsonplaceholder.typicode.com/", loggableHost = "jsonplaceholder.typicode.com"),
    )
    // syncForkConfig:access-points:end

    /** Resolve the access point declared under [id], or `null`. */
    fun byId(id: String): AccessPoint? = points.firstOrNull { it.id == id }

    /** Resolve the REST base URL registered for [type], or `null` if none is declared. */
    fun restBaseUrl(type: UrlType): String? =
        points.firstOrNull { it.type == type && it.kind == AccessPointKind.REST }?.baseUrl

    /** The first Supabase access point, or `null` if none is declared. */
    fun supabasePoint(): AccessPoint? =
        points.firstOrNull { it.kind == AccessPointKind.SUPABASE }

    /** Every declared host enabled for HTTP logging, across all access points. */
    fun loggableHosts(): List<String> = points.map { it.loggableHost }
}
