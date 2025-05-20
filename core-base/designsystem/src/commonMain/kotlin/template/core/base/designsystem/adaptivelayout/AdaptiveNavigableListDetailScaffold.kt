/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.designsystem.adaptivelayout

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.PaneExpansionState
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldScope
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalSharedTransitionApi::class,
)
@Composable
fun <T> AdaptiveNavigableListDetailPaneScaffold(
    items: List<T>,
    listPaneItem: @Composable (
        T,
        Boolean,
        Boolean,
        SharedTransitionScope,
        AnimatedVisibilityScope,
    ) -> Unit,
    detailPaneContent: @Composable (
        T,
        Boolean,
        Boolean,
        SharedTransitionScope,
        AnimatedVisibilityScope,
    ) -> Unit,
    modifier: Modifier = Modifier,
    extraPaneContent: @Composable (ThreePaneScaffoldScope.() -> Unit)? = null,
    paneExpansionDragHandle: @Composable (ThreePaneScaffoldScope.(PaneExpansionState) -> Unit)? = null,
    paneExpansionState: PaneExpansionState? = null,
) {
    var selectedItemIndex: Int? by rememberSaveable { mutableStateOf(null) }
    val navigator = rememberListDetailPaneScaffoldNavigator()
    val scope = rememberCoroutineScope()
    val isListAndDetailVisible =
        navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded &&
            navigator.scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Expanded

    BackHandler(enabled = navigator.canNavigateBack()) {
        scope.launch {
            navigator.navigateBack()
        }
    }

    SharedTransitionLayout {
        AnimatedContent(targetState = isListAndDetailVisible, label = "AdaptiveListDetailLayout") {
            ListDetailPaneScaffold(
                directive = navigator.scaffoldDirective,
                value = navigator.scaffoldValue,
                listPane = {
                    val currentSelectedWordIndex = selectedItemIndex
                    val isDetailVisible =
                        navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded
                    AnimatedPane {
                        ListContent(
                            items = items,
                            selectionState = if (isDetailVisible && currentSelectedWordIndex != null) {
                                SelectionVisibilityState.ShowSelection(currentSelectedWordIndex)
                            } else {
                                SelectionVisibilityState.NoSelection
                            },
                            onIndexClick = { index ->
                                selectedItemIndex = index
                                scope.launch {
                                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
                                }
                            },
                            isListAndDetailVisible = isListAndDetailVisible,
                            isListVisible = !isDetailVisible,
                            animatedVisibilityScope = this@AnimatedPane,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            listPaneItem = listPaneItem,
                        )
                    }
                },
                detailPane = {
                    val selectedItem = selectedItemIndex?.let(items::get)
                    val isDetailVisible =
                        navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded
                    AnimatedPane {
                        if (selectedItem != null) {
                            detailPaneContent(
                                selectedItem,
                                isListAndDetailVisible,
                                isDetailVisible,
                                this@SharedTransitionLayout,
                                this@AnimatedPane,
                            )
                        }
                    }
                },
                extraPane = extraPaneContent,
                modifier = modifier,
                paneExpansionDragHandle = paneExpansionDragHandle,
                paneExpansionState = paneExpansionState,
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun <T> ListContent(
    items: List<T>,
    selectionState: SelectionVisibilityState,
    onIndexClick: (index: Int) -> Unit,
    isListAndDetailVisible: Boolean,
    isListVisible: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    listPaneItem: @Composable (
        T,
        Boolean,
        Boolean,
        SharedTransitionScope,
        AnimatedVisibilityScope,
    ) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .then(
                when (selectionState) {
                    SelectionVisibilityState.NoSelection -> Modifier
                    is SelectionVisibilityState.ShowSelection -> Modifier.selectableGroup()
                },
            ),
    ) {
        itemsIndexed(items) { index, item ->

            val interactionModifier = when (selectionState) {
                SelectionVisibilityState.NoSelection -> {
                    Modifier.clickable(
                        onClick = { onIndexClick(index) },
                    )
                }

                is SelectionVisibilityState.ShowSelection -> {
                    Modifier.selectable(
                        selected = index == selectionState.selectedWordIndex,
                        onClick = { onIndexClick(index) },
                    )
                }
            }

            val containerColor = when (selectionState) {
                SelectionVisibilityState.NoSelection -> MaterialTheme.colorScheme.surface
                is SelectionVisibilityState.ShowSelection ->
                    if (index == selectionState.selectedWordIndex) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
            }
            val borderStroke = when (selectionState) {
                SelectionVisibilityState.NoSelection -> BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                )

                is SelectionVisibilityState.ShowSelection ->
                    if (index == selectionState.selectedWordIndex) {
                        null
                    } else {
                        BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                        )
                    }
            }

            // TODO: Card selection overfills the Card
            Card(
                colors = CardDefaults.cardColors(containerColor = containerColor),
                border = borderStroke,
                modifier = Modifier
                    .then(interactionModifier)
                    .fillMaxWidth(),
            ) {
                listPaneItem(
                    item,
                    isListAndDetailVisible,
                    isListVisible,
                    sharedTransitionScope,
                    animatedVisibilityScope,
                )
            }
        }
    }
}

/**
 * The description of the selection state for the [ListContent]
 */
sealed interface SelectionVisibilityState {

    /**
     * No selection should be shown, and each item should be clickable.
     */
    object NoSelection : SelectionVisibilityState

    /**
     * Selection state should be shown, and each item should be selectable.
     */
    data class ShowSelection(
        /**
         * The index of the word that is selected.
         */
        val selectedWordIndex: Int,
    ) : SelectionVisibilityState
}
