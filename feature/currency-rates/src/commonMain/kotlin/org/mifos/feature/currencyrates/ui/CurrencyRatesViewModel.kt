/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.currencyrates.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.mifos.core.data.currency.CurrencyRepository
import template.core.base.store.screen.ScreenState
import template.core.base.store.screen.combineContent
import template.core.base.store.screen.emptyIfContent
import template.core.base.ui.viewmodel.BaseViewModel

class CurrencyRatesViewModel(
    currencyRepository: CurrencyRepository,
) : BaseViewModel<RatesLocalState, Nothing, RatesAction>(RatesLocalState()) {

    private val stream = currencyRepository.exchangeRatesStream(
        baseCurrency = "USD",
        scope = viewModelScope,
    )

    val screenState: StateFlow<ScreenState<RatesDisplay>> = stream.state
        .combineContent(stateFlow) { rates, local, _ ->
            RatesDisplay(
                base = rates.base,
                date = rates.date,
                rates = rates.rates.filter {
                    local.searchQuery.isEmpty() || it.key.contains(local.searchQuery, true)
                },
            )
        }
        .emptyIfContent { it.rates.isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ScreenState.Loading)

    fun onRetry() {
        trySendAction(RatesAction.Retry)
    }

    fun onRefresh() {
        trySendAction(RatesAction.Refresh)
    }

    override fun handleAction(action: RatesAction) = when (action) {
        is RatesAction.Search -> updateState { copy(searchQuery = action.query) }
        RatesAction.Retry -> stream.retry()
        RatesAction.Refresh -> stream.refresh()
    }
}

data class RatesLocalState(val searchQuery: String = "")
data class RatesDisplay(val base: String, val date: String, val rates: Map<String, Double>)

sealed interface RatesAction {
    data class Search(val query: String) : RatesAction
    data object Retry : RatesAction
    data object Refresh : RatesAction
}
