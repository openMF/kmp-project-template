/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.watchlist.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import template.core.base.store.submit.SubmitState

/**
 * Star toggle that adds/removes a coin from the user's personal watchlist.
 *
 * Renders a filled star when the coin is in the watchlist, an outline star otherwise.
 * Tapping toggles via [AddToWatchlistViewModel] (which uses `SubmitHandler` — the
 * canonical simple-Mutation pattern). The button is disabled while a submit is in
 * flight to prevent double-tap races.
 *
 * @param coinId Identifier of the coin being toggled. One [AddToWatchlistViewModel]
 *   instance is created per coinId via Koin `parametersOf`.
 */
@Composable
fun AddToWatchlistButton(
    coinId: String,
    modifier: Modifier = Modifier,
    viewModel: AddToWatchlistViewModel = koinViewModel(key = coinId) { parametersOf(coinId) },
) {
    val isInWatchlist by viewModel.isInWatchlist.collectAsStateWithLifecycle(initialValue = false)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    IconButton(
        onClick = {
            viewModel.onSubmit(
                if (isInWatchlist) WatchlistMutation.Remove else WatchlistMutation.Add,
            )
        },
        modifier = modifier,
        enabled = uiState.submit !is SubmitState.Submitting,
    ) {
        Icon(
            imageVector = if (isInWatchlist) Icons.Filled.Star else Icons.Outlined.StarBorder,
            contentDescription = if (isInWatchlist) {
                "Remove $coinId from watchlist"
            } else {
                "Add $coinId to watchlist"
            },
        )
    }
}
