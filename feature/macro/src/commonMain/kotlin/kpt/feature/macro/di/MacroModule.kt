/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.macro.di

import kpt.core.model.demo.economic.IndicatorKind
import kpt.feature.macro.ui.CountryMacroViewModel
import kpt.feature.macro.ui.CountryPickerViewModel
import kpt.feature.macro.ui.MacroIndicatorDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for the Country Macro Snapshot feature.
 *
 * - [CountryMacroViewModel] takes the initial country code as a parameter so
 *   the navigation entry can pre-select (US by default; query-string-driven
 *   in deeplinks).
 * - [MacroIndicatorDetailViewModel] takes (countryCode, indicatorKind) as
 *   parameters — one VM per detail screen, scoped to the back-stack entry.
 * - [CountryPickerViewModel] has no parameters; it reads only from
 *   [kpt.core.data.demo.economic.SupportedCountries].
 */
val MacroModule = module {
    viewModel { (countryCode: String) ->
        CountryMacroViewModel(initialCountryCode = countryCode, repository = get())
    }
    viewModel { (countryCode: String, kind: IndicatorKind) ->
        MacroIndicatorDetailViewModel(
            countryCode = countryCode,
            indicatorKind = kind,
            repository = get(),
        )
    }
    viewModel { CountryPickerViewModel() }
}
