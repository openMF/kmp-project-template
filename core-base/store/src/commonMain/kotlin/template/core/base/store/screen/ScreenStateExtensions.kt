/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.store.screen

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import template.core.base.store.submit.SubmitHandler
import template.core.base.store.submit.SubmitState

/**
 * Transforms only [ScreenState.Content] data, passing through all other states unchanged.
 */
fun <T, R> Flow<ScreenState<T>>.mapContent(
    transform: (data: T, freshness: DataFreshness) -> R,
): Flow<ScreenState<R>> = map { state ->
    when (state) {
        is ScreenState.Content -> ScreenState.Content(
            data = transform(state.data, state.freshness),
            freshness = state.freshness,
            fetchedAt = state.fetchedAt,
        )
        is ScreenState.Loading -> ScreenState.Loading
        is ScreenState.Empty -> ScreenState.Empty
        is ScreenState.NoNetwork -> state
        is ScreenState.Error -> state
        is ScreenState.Unauthenticated -> state
    }
}

/**
 * Combines ScreenState with a local state flow for reactive filter/sort/preferences.
 */
fun <T, S, R> Flow<ScreenState<T>>.combineContent(
    other: Flow<S>,
    transform: (data: T, extra: S, freshness: DataFreshness) -> R,
): Flow<ScreenState<R>> = combine(this, other) { state, extra ->
    when (state) {
        is ScreenState.Content -> ScreenState.Content(
            data = transform(state.data, extra, state.freshness),
            freshness = state.freshness,
            fetchedAt = state.fetchedAt,
        )
        is ScreenState.Loading -> ScreenState.Loading
        is ScreenState.Empty -> ScreenState.Empty
        is ScreenState.NoNetwork -> state
        is ScreenState.Error -> state
        is ScreenState.Unauthenticated -> state
    }
}

/**
 * Converts Content to Empty when business-level predicate says data is empty.
 * Applied AFTER DecisionEngine (which only handles structural empty from Store).
 */
fun <T> Flow<ScreenState<T>>.emptyIfContent(
    predicate: (T) -> Boolean,
): Flow<ScreenState<T>> = map { state ->
    when (state) {
        is ScreenState.Content -> if (predicate(state.data)) ScreenState.Empty else state
        else -> state
    }
}

/** Extracts data from Content state or null for other states. */
val <T> ScreenState<T>.dataOrNull: T?
    get() = (this as? ScreenState.Content)?.data

/** True if state has displayable content. */
val <T> ScreenState<T>.hasContent: Boolean
    get() = this is ScreenState.Content

/** Maps Error throwable to a user-facing type. */
fun <T> Flow<ScreenState<T>>.mapError(
    transform: (Throwable) -> Throwable,
): Flow<ScreenState<T>> = map { state ->
    when (state) {
        is ScreenState.Error -> ScreenState.Error(
            error = transform(state.error),
            isNetworkError = state.isNetworkError,
        )
        else -> state
    }
}

/**
 * Execute [block] only when [screenState] has loaded content, passing its data.
 * No-op when state is Loading / Error / NoNetwork / Empty — safe to call at any time.
 * Guards against premature submits before data arrives on edit screens.
 */
fun <T, R> SubmitHandler<R>.submitWhenContent(
    screenState: ScreenState<T>,
    block: suspend (data: T) -> R,
) {
    val data = screenState.dataOrNull ?: return
    submit { block(data) }
}

/**
 * True when the screen has content AND no submission is in-flight.
 *
 * Use to enable a submit button without collecting two flows separately:
 * ```kotlin
 * Button(enabled = screenState.canInteract(submitState)) { … }
 * ```
 */
fun <T, R> ScreenState<T>.canInteract(submitState: SubmitState<R>): Boolean =
    hasContent && submitState !is SubmitState.Submitting

/**
 * Combines two independent [ScreenState] flows into one, merging their [ScreenState.Content]
 * data via [transform] when both sources have loaded.
 *
 * **Priority** (first match wins on each emission):
 * `NoNetwork > Loading > Unauthenticated > Error > Empty > Content`
 *
 * Both sources must reach [ScreenState.Content] before [transform] is called.
 * The resulting [DataFreshness] is the "worst" of the two:
 * STALE if either is STALE; UPDATING if either is UPDATING; FRESH only when both are FRESH.
 *
 * Typical use — dashboard screen combining two independent data sources:
 * ```kotlin
 * val screenState: Flow<ScreenState<DashboardUiState>> = combineScreenStates(
 *     a = accountStore.asScreenStream(Unit),
 *     b = transactionStore.asScreenStream(Unit),
 * ) { account, transactions ->
 *     DashboardUiState(account = account, transactions = transactions)
 * }
 * ```
 */
@OptIn(ExperimentalTime::class)
fun <A, B, R> combineScreenStates(
    a: Flow<ScreenState<A>>,
    b: Flow<ScreenState<B>>,
    transform: (A, B) -> R,
): Flow<ScreenState<R>> = combine(a, b) { stateA, stateB ->
    when {
        stateA is ScreenState.NoNetwork || stateB is ScreenState.NoNetwork ->
            ScreenState.NoNetwork()
        stateA is ScreenState.Loading || stateB is ScreenState.Loading ->
            ScreenState.Loading
        stateA is ScreenState.Unauthenticated || stateB is ScreenState.Unauthenticated ->
            ScreenState.Unauthenticated
        stateA is ScreenState.Error -> stateA
        stateB is ScreenState.Error -> stateB
        stateA is ScreenState.Empty || stateB is ScreenState.Empty ->
            ScreenState.Empty
        stateA is ScreenState.Content && stateB is ScreenState.Content ->
            ScreenState.Content(
                data = transform(stateA.data, stateB.data),
                freshness = combineFreshness(stateA.freshness, stateB.freshness),
                fetchedAt = minFetchedAt(stateA.fetchedAt, stateB.fetchedAt),
            )
        else -> ScreenState.Loading
    }
}

@OptIn(ExperimentalTime::class)
private fun combineFreshness(a: DataFreshness, b: DataFreshness): DataFreshness = when {
    a == DataFreshness.STALE || b == DataFreshness.STALE -> DataFreshness.STALE
    a == DataFreshness.UPDATING || b == DataFreshness.UPDATING -> DataFreshness.UPDATING
    else -> DataFreshness.FRESH
}

@OptIn(ExperimentalTime::class)
private fun minFetchedAt(a: Instant?, b: Instant?): Instant? = when {
    a == null || b == null -> null
    else -> if (a < b) a else b
}
