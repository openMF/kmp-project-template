/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.model.economic

/**
 * Country reference for the Banking Utility Toolkit's macro-indicator screens.
 *
 * Distinct from [org.mifos.core.model.currency.Country] which is a
 * phone-number-formatting model. The toolkit screens need only the World
 * Bank-accepted ISO 3166-1 alpha-2 code plus a localised display name and a
 * flag emoji — no phone metadata.
 *
 * @property code ISO 3166-1 alpha-2, e.g. `"US"`, `"IN"`, `"DE"`. Forwarded
 *   unchanged to [org.mifos.core.network.economic.api.WorldBankApi.indicator].
 * @property name English short name, e.g. `"United States"`, `"India"`.
 * @property flagEmoji Unicode flag glyph composed from the regional-indicator
 *   pair for [code]. Falls back to a globe emoji for non-country regions if
 *   ever needed (currently every entry resolves to a valid flag).
 */
data class Country(
    val code: String,
    val name: String,
    val flagEmoji: String,
)
