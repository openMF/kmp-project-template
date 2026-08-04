/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.watchlist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.data.demo.watchlist.WatchlistItem
import kpt.feature.watchlist.generated.resources.Res
import kpt.feature.watchlist.generated.resources.screens_watchlist_back_cd
import kpt.feature.watchlist.generated.resources.screens_watchlist_remove_cd
import kpt.feature.watchlist.generated.resources.screens_watchlist_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Watchlist — the toolkit's `read_local_list` demo. Renders the user's saved coins
 * as an offline Room-backed reactive list (non-paged). Each row offers a remove
 * action; the list re-emits instantly as Room propagates the change. No network.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WatchlistViewModel = koinViewModel(),
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.testTag(TestTags.Watchlist.SCREEN),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.screens_watchlist_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.screens_watchlist_back_cd),
                        )
                    }
                },
            )
        },
    ) { padding ->
        ScreenContent(
            state = state,
            onRetry = { },
            modifier = Modifier.fillMaxSize().padding(padding),
        ) { items, _ ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
            ) {
                items(items = items, key = WatchlistItem::coinId) { item ->
                    WatchlistRow(item = item, onRemove = { viewModel.onRemove(item.coinId) })
                }
            }
        }
    }
}

@Composable
private fun WatchlistRow(
    item: WatchlistItem,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier.fillMaxWidth().testTag(TestTags.Watchlist.ROW_PREFIX + item.coinId),
        headlineContent = { Text(item.coinId) },
        trailingContent = {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.testTag(TestTags.Watchlist.REMOVE_PREFIX + item.coinId),
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(Res.string.screens_watchlist_remove_cd),
                )
            }
        },
    )
}
