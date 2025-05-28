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
import template.core.base.designsystem.component.KptCard
import template.core.base.designsystem.component.KptScaffold
import template.core.base.designsystem.component.KptSlider
import template.core.base.designsystem.component.KptTopAppBar
import template.core.base.designsystem.component.SliderConfiguration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KptSliderCatalogScreen(
    navigateBack: () -> Unit,
) {
    KptScaffold(
        topBar = {
            KptTopAppBar(
                title = "KptSlider Catalog",
                onNavigationIconClick = navigateBack,
            )
        },
    ) {
        var selectedDemo by remember { mutableStateOf<SliderDemoType?>(null) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "KptSlider Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            items(items = SliderDemoType.entries.toList()) { demoType ->
                SliderCatalogItem(
                    demoType = demoType,
                    onClick = { selectedDemo = demoType },
                )
            }
        }

        // Show the selected slider demo
        when (selectedDemo) {
            SliderDemoType.BASIC -> BasicSliderExample()
            SliderDemoType.LABEL -> LabelSliderExample()
            SliderDemoType.RANGE_STEPS -> RangeStepsSliderExample()
            SliderDemoType.DISABLED -> DisabledSliderExample()
            SliderDemoType.LABEL_FORMATTER -> LabelFormatterSliderExample()
            null -> {}
        }
    }
}

@Composable
private fun SliderCatalogItem(
    demoType: SliderDemoType,
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
                    text = demoType.title,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = demoType.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private enum class SliderDemoType(
    val title: String,
    val description: String,
) {
    BASIC("Basic Slider", "KptSlider with default range and value"),
    LABEL("Slider with Label", "KptSlider with label above the slider"),
    RANGE_STEPS("Slider with Range & Steps", "KptSlider with custom value range and steps"),
    DISABLED("Disabled Slider", "KptSlider in a disabled state"),
    LABEL_FORMATTER("Slider with Label Formatter", "KptSlider with custom label formatting"),
}

@Composable
private fun BasicSliderExample() {
    var value by remember { mutableStateOf(0.5f) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptSlider(
            configuration = SliderConfiguration(
                value = value,
                onValueChange = { value = it },
            ),
        )
    }
}

@Composable
private fun LabelSliderExample() {
    var value by remember { mutableStateOf(0.3f) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptSlider(
            configuration = SliderConfiguration(
                value = value,
                onValueChange = { value = it },
                showLabel = true,
            ),
        )
    }
}

@Composable
private fun RangeStepsSliderExample() {
    var value by remember { mutableStateOf(5f) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptSlider(
            configuration = SliderConfiguration(
                value = value,
                onValueChange = { value = it },
                valueRange = 0f..10f,
                steps = 4,
                showLabel = true,
            ),
        )
    }
}

@Composable
private fun DisabledSliderExample() {
    var value by remember { mutableStateOf(0.7f) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptSlider(
            configuration = SliderConfiguration(
                value = value,
                onValueChange = { value = it },
                enabled = false,
                showLabel = true,
            ),
        )
    }
}

@Composable
private fun LabelFormatterSliderExample() {
    var value by remember { mutableStateOf(0.25f) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptSlider(
            configuration = SliderConfiguration(
                value = value,
                onValueChange = { value = it },
                showLabel = true,
                labelFormatter = { v -> "${(v * 100).toInt()}%" },
            ),
        )
    }
}
