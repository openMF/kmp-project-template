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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kpt.core.base.store.freshness.FreshnessBand
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.economic.MacroIndicatorsRepository
import kpt.core.model.economic.IndicatorKind
import kpt.core.model.economic.MacroIndicator
import kpt.core.store.economic.impl.MacroIndicatorKey

/**
 * Country Macro Snapshot ViewModel.
 *
 * Multi-source-combine showcase: holds **three independent**
 * [ScreenDataStream]s (GDP, Inflation, Unemployment) and exposes their states
 * as three independent cells on [MacroUiState]. One failing indicator must
 * NOT take the other two down with it — each card retries independently.
 *
 * On [MacroAction.ChangeCountry], the three subscriptions are cancelled and
 * re-created keyed on the new country, so emissions from the previous
 * country's streams cannot bleed into the post-switch UI.
 *
 * @param initialCountryCode ISO 3166-1 alpha-2 — typically `"US"` from the
 *   navigation default; users override via the country picker.
 * @param repository Source of the World Bank-backed [ScreenDataStream]s.
 */
class CountryMacroViewModel(
    initialCountryCode: String,
    private val repository: MacroIndicatorsRepository,
) : BaseViewModel<MacroUiState, Nothing, MacroAction>(
    MacroUiState(countryCode = initialCountryCode),
) {

    private val streams: MutableMap<IndicatorKind, ScreenDataStream<MacroIndicator>> =
        mutableMapOf()
    private val subscriptionJobs: MutableMap<IndicatorKind, Job> = mutableMapOf()

    init {
        subscribeAll(initialCountryCode)
    }

    private fun subscribeAll(countryCode: String) {
        // Cancel any prior subscriptions so the new country's emissions take
        // over cleanly and the UI never shows stale-country data.
        subscriptionJobs.values.forEach { it.cancel() }
        subscriptionJobs.clear()
        streams.clear()

        TRACKED_INDICATORS.forEach { kind ->
            val key = MacroIndicatorKey(countryCode = countryCode, indicator = kind)
            val stream = repository.macroIndicatorStream(key = key, scope = viewModelScope)
            streams[kind] = stream
            // Reset the per-cell state to Loading on re-subscription so the
            // card visibly indicates "fetching for new country" instead of
            // freezing the previous country's content.
            updateState { withIndicator(kind, ScreenState.Loading) }
            subscriptionJobs[kind] = stream.state
                .onEach { state -> updateState { withIndicator(kind, state) } }
                .launchIn(viewModelScope)
        }
    }

    override fun handleAction(action: MacroAction) {
        when (action) {
            MacroAction.RefreshAll -> streams.values.forEach { it.refresh() }
            MacroAction.RetryGdp -> streams[IndicatorKind.GDP]?.retry()
            MacroAction.RetryInflation -> streams[IndicatorKind.INFLATION_CPI]?.retry()
            MacroAction.RetryUnemployment -> streams[IndicatorKind.UNEMPLOYMENT]?.retry()
            is MacroAction.ChangeCountry -> {
                if (action.code == state.countryCode) return
                updateState { copy(countryCode = action.code) }
                subscribeAll(action.code)
            }
        }
    }

    /** Re-derive a [MacroUiState] with the named indicator's cell replaced. */
    private fun MacroUiState.withIndicator(
        kind: IndicatorKind,
        state: ScreenState<MacroIndicator>,
    ): MacroUiState = when (kind) {
        IndicatorKind.GDP -> copy(gdp = state)
        IndicatorKind.INFLATION_CPI -> copy(inflation = state)
        IndicatorKind.UNEMPLOYMENT -> copy(unemployment = state)
        // The toolkit's two extra IndicatorKinds (GDP_PER_CAPITA, GINI) are
        // not surfaced on this screen; ignore so the compiler exhaustiveness
        // check stays honest if the enum expands later.
        IndicatorKind.GDP_PER_CAPITA, IndicatorKind.GINI -> this
    }

    companion object {
        /** Indicators rendered on the Country Macro Snapshot dashboard. */
        val TRACKED_INDICATORS: List<IndicatorKind> = listOf(
            IndicatorKind.GDP,
            IndicatorKind.INFLATION_CPI,
            IndicatorKind.UNEMPLOYMENT,
        )
    }
}

/**
 * Aggregated UI state for the country-macro dashboard.
 *
 * Each indicator is its own [ScreenState] so the screen can render three
 * cards in three independent states (e.g., GDP showing content while
 * inflation is still loading). The aggregate freshness lets the screen
 * surface a single overall STALE badge in the TopAppBar without callers
 * having to manually fold the three.
 */
data class MacroUiState(
    val countryCode: String,
    val gdp: ScreenState<MacroIndicator> = ScreenState.Loading,
    val inflation: ScreenState<MacroIndicator> = ScreenState.Loading,
    val unemployment: ScreenState<MacroIndicator> = ScreenState.Loading,
) {
    /**
     * Worst-case freshness band across the three indicator cells.
     *
     * VeryStale > Stale > Fresh > Initial — cells not in Content (Loading / Error /
     * Empty / NoNetwork) do not contribute. The screen's freshness badge is only
     * meaningful once at least one indicator has surfaced data.
     */
    val overallBand: FreshnessBand
        get() {
            val bands = listOfNotNull(
                (gdp as? ScreenState.Content<*>)?.freshnessSignal?.band,
                (inflation as? ScreenState.Content<*>)?.freshnessSignal?.band,
                (unemployment as? ScreenState.Content<*>)?.freshnessSignal?.band,
            )
            if (bands.isEmpty()) return FreshnessBand.Fresh
            return when {
                bands.any { it == FreshnessBand.VeryStale } -> FreshnessBand.VeryStale
                bands.any { it == FreshnessBand.Stale } -> FreshnessBand.Stale
                bands.any { it == FreshnessBand.Initial } -> FreshnessBand.Initial
                else -> FreshnessBand.Fresh
            }
        }
}

/** User intents the country-macro dashboard accepts. */
sealed interface MacroAction {
    /** Refresh all 3 indicator streams in parallel (pull-to-refresh UX). */
    data object RefreshAll : MacroAction

    /** Retry only the GDP card after an Error / NoNetwork state. */
    data object RetryGdp : MacroAction

    /** Retry only the Inflation card. */
    data object RetryInflation : MacroAction

    /** Retry only the Unemployment card. */
    data object RetryUnemployment : MacroAction

    /** Switch the dashboard to a new country — re-keys all 3 streams. */
    data class ChangeCountry(val code: String) : MacroAction
}
