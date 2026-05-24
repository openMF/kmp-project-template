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

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.mifos.core.data.economic.MacroIndicatorsRepository
import org.mifos.core.model.economic.IndicatorKind
import org.mifos.core.model.economic.MacroIndicator
import org.mifos.core.store.economic.impl.MacroIndicatorKey
import template.core.base.store.screen.ScreenDataStream
import template.core.base.store.screen.ScreenState
import template.core.base.ui.viewmodel.BaseViewModel

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

    private val stream: ScreenDataStream<MacroIndicator> = repository.macroIndicatorStream(
        key = MacroIndicatorKey(countryCode = countryCode, indicator = indicatorKind),
        scope = viewModelScope,
    )

    val screenState: StateFlow<ScreenState<MacroIndicator>> = stream.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState.Loading)

    fun onRetry() {
        trySendAction(MacroDetailAction.Retry)
    }

    override fun handleAction(action: MacroDetailAction) = when (action) {
        MacroDetailAction.Retry -> stream.retry()
        MacroDetailAction.Refresh -> stream.refresh()
    }
}

sealed interface MacroDetailAction {
    data object Retry : MacroDetailAction
    data object Refresh : MacroDetailAction
}
