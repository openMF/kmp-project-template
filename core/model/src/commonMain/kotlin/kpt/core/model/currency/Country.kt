/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.currency

/**
 * A currency-bearing country: ISO code, display name and the currency it uses.
 */
data class Country(
    /** ISO 3166-1 alpha-2 code, e.g. `US`. */
    val code: String,
    /** English display name. */
    val name: String,
    /** International dialling prefix without the `+`, e.g. `1`. */
    val phoneCode: String,
    /** Regex a mobile number must match in this country. */
    val mobilePattern: String,
    /** Regex for landlines, or null where the mobile pattern covers both. */
    val landlinePattern: String? = null,
    /** Regex for short/service numbers, or null when there are none to accept. */
    val specialPattern: String? = null,
    /** A correctly formatted sample number, shown as input hint text. */
    val formatExample: String,
    /** Flag as an emoji — the fallback where the bundled asset is missing. */
    val flagEmoji: String,
    /**
     * Name of the bundled flag drawable, without extension. Resolved at render time, so a country whose asset is
     * missing falls back to [flagEmoji] rather than rendering blank.
     */
    val flagResourceName: String,
)
