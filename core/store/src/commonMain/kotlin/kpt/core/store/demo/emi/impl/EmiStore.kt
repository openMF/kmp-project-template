/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.demo.emi.impl

import kpt.core.base.store.infra.StoreFactory
import kpt.core.model.demo.emi.EmiResult
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.Store

/**
 * The Store key for a single EMI computation — the calculator's inputs.
 *
 * A calculator has no remote resource to key on, so the INPUTS are the key: two identical
 * parameter sets are the same cache entry, and changing any field is a new entry. That is
 * what makes memoisation fall out of the Store rather than being hand-rolled in a ViewModel.
 */
data class EmiParams(
    val principal: Double,
    val ratePercent: Double,
    val tenureMonths: Int,
) {
    /** True when the inputs describe a computable loan — the store is only read when valid. */
    val isComputable: Boolean
        get() = principal > 0 && ratePercent > 0 && tenureMonths > 0
}

/**
 * The compute PORT for the EMI store.
 *
 * `core/store` cannot import `core/domain`: `core/domain` declares `api(projects.core.data)`
 * and `core/data` declares `api(projects.core.store)`, so a direct import would close a
 * dependency cycle (store → domain → data → store). The store therefore owns the interface
 * and the FEATURE module binds the real `calculateEmi` implementation into Koin — the
 * dependency is satisfied at runtime, not at compile time, and the layer order (store below
 * domain) is preserved.
 */
fun interface EmiCompute {
    suspend operator fun invoke(params: EmiParams): EmiResult
}

/**
 * MEMORY_ONLY Store5 store over a pure computation (`feature_profile.combo_id: calculator_pure`).
 *
 * There is no network and no source of truth, so the Store contributes exactly one thing:
 * an in-memory cache keyed on [EmiParams]. Re-entering a parameter set the user already tried
 * — the common case while dragging a tenure slider back and forth — is served from cache
 * instead of recomputed, and the result reaches the screen as a `ScreenState` like every other
 * read surface, so the calculator renders through the same `ScreenContent` wrapper as the rest
 * of the app rather than a bespoke nullable `StateFlow`.
 *
 * MEMORY_ONLY is only legal because no `cache_strategy` is declared for this feature (SC2).
 */
fun provideEmiStore(compute: EmiCompute): Store<EmiParams, EmiResult> =
    StoreFactory.createMemoryStore(
        fetcher = Fetcher.of { params: EmiParams -> compute(params) },
    )
