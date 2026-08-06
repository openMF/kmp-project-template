/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.addtowatchlist.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.feature.addtowatchlist.generated.resources.Res
import kpt.feature.addtowatchlist.generated.resources.screens_watchlist_star_add_cd
import kpt.feature.addtowatchlist.generated.resources.screens_watchlist_star_remove_cd
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * The embedded WRITE-side widget of the Personal Crypto Watchlist — a single star
 * icon-button, NOT a navigable screen. Host it on any coin surface (`feature/crypto`
 * CoinMarkets rows + Coin Detail) by passing the row's `coinId`.
 *
 * Renders `Star` (filled) when the coin is tracked, `StarBorder` (outline) otherwise, from
 * `repository.contains(coinId)`. Tapping toggles membership via [AddToWatchlistViewModel]'s
 * SubmitHandler; the local Room commit re-emits `contains()` and the star flips instantly.
 *
 * The VM is keyed by `coinId` (`koinViewModel(key = coinId)` + `parametersOf(coinId)`) so
 * every hosting row owns an independent toggle.
 */
@Composable
fun AddToWatchlistStar(
    coinId: String,
    modifier: Modifier = Modifier,
    viewModel: AddToWatchlistViewModel = koinViewModel(key = coinId) { parametersOf(coinId) },
) {
    val isTracked by viewModel.isTracked.collectAsStateWithLifecycle()

    IconButton(
        onClick = viewModel::onToggle,
        modifier = modifier.testTag(TestTags.AddToWatchlist.STAR_PREFIX + coinId),
    ) {
        Icon(
            imageVector = if (isTracked) Icons.Filled.Star else Icons.Outlined.StarBorder,
            contentDescription = stringResource(
                if (isTracked) {
                    Res.string.screens_watchlist_star_remove_cd
                } else {
                    Res.string.screens_watchlist_star_add_cd
                },
            ),
        )
    }
}
