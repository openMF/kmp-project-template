/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.paging

import kotlinx.serialization.Serializable

/**
 * Store-level durable cursor for a [PagingScreenStream].
 *
 * Distinct from the datastore-side `ScreenUiState`'s nested `PagingCursor`
 * for two reasons: (a) `core-base/store` must not depend on `core-base/datastore`
 * (the current dep is `datastore -> ui -> store`, never the reverse); (b) the
 * store-level cursor carries the full restore tuple `{lastPageLoaded, scrollIndex,
 * scrollOffset, query}` so a caller that wires paging without any datastore layer
 * (tests, forks with a bespoke persistence stack) still has a single value type to
 * observe. Consumers wiring through `ScreenUiStateStore` map the two axes at the
 * feature seam — see `feature/crypto/CoinMarketsViewModel` for the canonical
 * mapping.
 *
 * All fields default to zero / null so callers can seed via [EMPTY] without
 * naming every parameter.
 */
@Serializable
data class PagingCursor(
    val lastPageLoaded: Int = 0,
    val scrollIndex: Int = 0,
    val scrollOffset: Int = 0,
    val query: String? = null,
) {
    companion object {
        val EMPTY: PagingCursor = PagingCursor()
    }
}
