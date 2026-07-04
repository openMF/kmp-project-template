/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.datastore.screen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Compact, `@Serializable` record persisted per-screen by [ScreenUiStateStore].
 *
 * Snake-case `@SerialName` keeps the on-disk contract stable across future
 * refactors while the Kotlin identifiers stay camelCase. Every field carries
 * a sensible default so [ScreenUiStateStore.update] can start from
 * `ScreenUiState()` on first write for a route.
 *
 * See GOAL AC-5 for the field set.
 */
@Serializable
data class ScreenUiState(
    @SerialName("scroll_index") val scrollIndex: Int = 0,
    @SerialName("scroll_offset") val scrollOffset: Int = 0,
    @SerialName("filter") val filter: String? = null,
    @SerialName("tab") val tab: String? = null,
    @SerialName("search") val search: String? = null,
    @SerialName("expanded_ids") val expandedIds: List<String> = emptyList(),
    @SerialName("selection_ids") val selectionIds: List<String> = emptyList(),
    @SerialName("paging_cursor") val pagingCursor: PagingCursor? = null,
    @SerialName("updated_at") val updatedAt: Long = 0L,
)

/**
 * Optional paging-cursor sub-record — carried in [ScreenUiState] so screens that
 * use [kpt.core.base.store.paging.PagingScreenStream] can persist the cursor for
 * deep-scroll restore. Phase 2 only carries the field; Phase 4+ wires the runtime
 * that reads it back into `PagingScreenStream`.
 */
@Serializable
data class PagingCursor(
    @SerialName("last_page_loaded") val lastPageLoaded: Int = 0,
    @SerialName("query") val query: String? = null,
)
