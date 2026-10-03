/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.macro.ui

import androidx.lifecycle.viewModelScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.economic.MacroIndicatorsRepository
import kpt.core.model.economic.IndicatorKind
import kpt.core.model.economic.MacroIndicator
import kpt.core.store.economic.impl.MacroIndicatorKey

/**
 * Deep-dive ViewModel for a single (country, indicator) tuple.
 *
 * Used by the indicator-detail screen — full historical series, table view,
 * single retry. The list/dashboard ViewModel ([CountryMacroViewModel])
 * combines three of these conceptually, but each card on that dashboard is
 * a coarse summary; the detail screen is where users see every observation
 * year for one indicator.
 */
class MacroIndicatorDetailViewModel(
    countryCode: String,
    indicatorKind: IndicatorKind,
    repository: MacroIndicatorsRepository,
) : BaseViewModel<Unit, Nothing, MacroDetailAction>(Unit) {

    /** The repository-built stream — the screen renders it directly via `ScreenContent(stream)`. */
    val indicator: ScreenDataStream<MacroIndicator> = repository.macroIndicatorStream(
        key = MacroIndicatorKey(countryCode = countryCode, indicator = indicatorKind),
        scope = viewModelScope,
    )

    /** Retries the failed fetch. */
    fun onRetry() {
        trySendAction(MacroDetailAction.Retry)
    }

    override fun handleAction(action: MacroDetailAction) = when (action) {
        MacroDetailAction.Retry -> indicator.retry()
        MacroDetailAction.Refresh -> indicator.refresh()
    }
}

/** What the detail screen can be asked to do. */
sealed interface MacroDetailAction {
    /** Retry the failed fetch. */
    data object Retry : MacroDetailAction

    /** Force a fetch, bypassing the cache. */
    data object Refresh : MacroDetailAction
}
