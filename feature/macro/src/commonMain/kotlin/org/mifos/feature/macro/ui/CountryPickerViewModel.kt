/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.macro.ui

import org.mifos.core.data.economic.SupportedCountries
import org.mifos.core.model.economic.Country
import template.core.base.ui.viewmodel.BaseViewModel

/**
 * Pure-local picker over [SupportedCountries].
 *
 * The list is static and bundled at compile time — no Store, no network, no
 * loading state. Search filters via [SupportedCountries.search] which
 * matches either an ISO-code prefix or a substring of the localised name.
 *
 * No `Country` ScreenState here: the picker has nothing that can fail, so
 * the framework's ScreenState model would add ceremony for zero gain.
 */
class CountryPickerViewModel : BaseViewModel<CountryPickerState, Nothing, CountryPickerAction>(
    CountryPickerState(searchQuery = "", results = SupportedCountries.list),
) {

    override fun handleAction(action: CountryPickerAction) = when (action) {
        is CountryPickerAction.Search -> updateState {
            copy(
                searchQuery = action.query,
                results = SupportedCountries.search(action.query),
            )
        }
    }
}

/**
 * Picker UI state.
 *
 * @property searchQuery The verbatim text the user has typed; round-tripped
 *   into the search field so the cursor doesn't reset on each emission.
 * @property results Countries matching [searchQuery] (or the full curated
 *   list when [searchQuery] is empty / whitespace-only).
 */
data class CountryPickerState(
    val searchQuery: String,
    val results: List<Country>,
)

/** User intents the picker accepts. */
sealed interface CountryPickerAction {
    /** Free-text search; empty restores the full list. */
    data class Search(val query: String) : CountryPickerAction
}
