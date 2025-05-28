/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(ExperimentalMaterial3Api::class)

package org.mifos.feature.home.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import template.core.base.designsystem.component.ChipConfiguration
import template.core.base.designsystem.component.KptAssistChip
import template.core.base.designsystem.component.KptCard
import template.core.base.designsystem.component.KptChip
import template.core.base.designsystem.component.KptFilterChip
import template.core.base.designsystem.component.KptScaffold
import template.core.base.designsystem.component.KptTopAppBar
import template.core.base.designsystem.core.ChipVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KptChipCatalogScreen(
    navigateBack: () -> Unit,
) {
    KptScaffold(
        topBar = {
            KptTopAppBar(
                title = "KptChip Catalog",
                onNavigationIconClick = navigateBack,
            )
        },
    ) {
        var selectedChip by remember { mutableStateOf<ChipType?>(null) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "KptChip Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            items(items = ChipType.entries.toList()) { chipType ->
                ChipCatalogItem(
                    chipType = chipType,
                    onClick = { selectedChip = chipType },
                )
            }
        }

        // Show the selected chip demo
        when (selectedChip) {
            ChipType.ASSIST -> AssistChipExample()
            ChipType.FILTER -> FilterChipExample()
            ChipType.INPUT -> InputChipExample()
            ChipType.SUGGESTION -> SuggestionChipExample()
            null -> {}
        }
    }
}

@Composable
private fun ChipCatalogItem(
    chipType: ChipType,
    onClick: () -> Unit,
) {
    KptCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = chipType.title,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = chipType.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private enum class ChipType(
    val title: String,
    val description: String,
) {
    ASSIST(
        title = "Assist Chip",
        description = "KptAssistChip with label and optional icon",
    ),
    FILTER(
        title = "Filter Chip",
        description = "KptFilterChip with selectable state",
    ),
    INPUT(
        title = "Input Chip",
        description = "KptChip with Input variant and selectable state",
    ),
    SUGGESTION(
        title = "Suggestion Chip",
        description = "KptChip with Suggestion variant",
    ),
}

@Composable
private fun AssistChipExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptAssistChip(
            label = "Assist Chip",
            onClick = {},
        )
    }
}

@Composable
private fun FilterChipExample() {
    var selected by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptFilterChip(
            label = "Filter Chip",
            selected = selected,
            onClick = { selected = !selected },
        )
    }
}

@Composable
private fun InputChipExample() {
    var selected by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptChip(
            configuration = ChipConfiguration(
                variant = ChipVariant.Input,
                label = "Input Chip",
                selected = selected,
                onClick = { selected = !selected },
            ),
        )
    }
}

@Composable
private fun SuggestionChipExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptChip(
            configuration = ChipConfiguration(
                variant = ChipVariant.Suggestion,
                label = "Suggestion Chip",
                onClick = {},
            ),
        )
    }
}
