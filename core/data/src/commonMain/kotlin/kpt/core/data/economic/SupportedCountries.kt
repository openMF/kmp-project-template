/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.economic

import kpt.core.model.economic.Country

/**
 * Curated list of countries the Banking Utility Toolkit's macro-snapshot
 * screen ships with out of the box.
 *
 * Selection criteria:
 * - The G20 plus a handful of other large economies the template's adopters
 *   are most likely to demo against. This gives the screen a useful default
 *   experience without bundling the full ~250-country World Bank catalogue.
 * - Codes are ISO 3166-1 alpha-2, the form the toolkit standardises on (see
 *   [kpt.core.model.economic.MacroIndicator]). The World Bank API
 *   accepts alpha-2 directly.
 *
 * Future enhancement: dynamic loading via the World Bank `/v2/country`
 * endpoint is out of scope for this template — a hardcoded curated list is
 * sufficient and keeps the picker snappy with no extra request.
 */
object SupportedCountries {

    /** Curated G20-plus list — alphabetised by display name for stable UX. */
    val list: List<Country> = listOf(
        Country(code = "AR", name = "Argentina", flagEmoji = "🇦🇷"),
        Country(code = "AU", name = "Australia", flagEmoji = "🇦🇺"),
        Country(code = "BR", name = "Brazil", flagEmoji = "🇧🇷"),
        Country(code = "CA", name = "Canada", flagEmoji = "🇨🇦"),
        Country(code = "CN", name = "China", flagEmoji = "🇨🇳"),
        Country(code = "EG", name = "Egypt", flagEmoji = "🇪🇬"),
        Country(code = "FR", name = "France", flagEmoji = "🇫🇷"),
        Country(code = "DE", name = "Germany", flagEmoji = "🇩🇪"),
        Country(code = "IN", name = "India", flagEmoji = "🇮🇳"),
        Country(code = "ID", name = "Indonesia", flagEmoji = "🇮🇩"),
        Country(code = "IT", name = "Italy", flagEmoji = "🇮🇹"),
        Country(code = "JP", name = "Japan", flagEmoji = "🇯🇵"),
        Country(code = "KE", name = "Kenya", flagEmoji = "🇰🇪"),
        Country(code = "KR", name = "Korea, Rep.", flagEmoji = "🇰🇷"),
        Country(code = "MX", name = "Mexico", flagEmoji = "🇲🇽"),
        Country(code = "NG", name = "Nigeria", flagEmoji = "🇳🇬"),
        Country(code = "PK", name = "Pakistan", flagEmoji = "🇵🇰"),
        Country(code = "PH", name = "Philippines", flagEmoji = "🇵🇭"),
        Country(code = "PL", name = "Poland", flagEmoji = "🇵🇱"),
        Country(code = "RU", name = "Russian Federation", flagEmoji = "🇷🇺"),
        Country(code = "SA", name = "Saudi Arabia", flagEmoji = "🇸🇦"),
        Country(code = "SG", name = "Singapore", flagEmoji = "🇸🇬"),
        Country(code = "ZA", name = "South Africa", flagEmoji = "🇿🇦"),
        Country(code = "ES", name = "Spain", flagEmoji = "🇪🇸"),
        Country(code = "SE", name = "Sweden", flagEmoji = "🇸🇪"),
        Country(code = "CH", name = "Switzerland", flagEmoji = "🇨🇭"),
        Country(code = "TR", name = "Turkiye", flagEmoji = "🇹🇷"),
        Country(code = "AE", name = "United Arab Emirates", flagEmoji = "🇦🇪"),
        Country(code = "GB", name = "United Kingdom", flagEmoji = "🇬🇧"),
        Country(code = "US", name = "United States", flagEmoji = "🇺🇸"),
        Country(code = "VN", name = "Vietnam", flagEmoji = "🇻🇳"),
    )

    /** Backing index keyed on uppercase ISO code for O(1) [findByCode]. */
    private val byCode: Map<String, Country> = list.associateBy { it.code }

    /**
     * Lookup a country by ISO code. Case-insensitive — the picker, intent
     * deeplinks, or saved navigation args may all carry mixed case.
     */
    fun findByCode(code: String): Country? = byCode[code.trim().uppercase()]

    /**
     * Filter the list by a free-text query. Matches a country's ISO code as a
     * prefix OR its localised name as a substring, both case-insensitively.
     *
     * An empty (or whitespace-only) query returns the full list — typical
     * picker behavior so the screen renders before the user types anything.
     */
    fun search(query: String): List<Country> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return list
        val upper = trimmed.uppercase()
        return list.filter { c ->
            c.code.startsWith(upper) || c.name.contains(trimmed, ignoreCase = true)
        }
    }
}
