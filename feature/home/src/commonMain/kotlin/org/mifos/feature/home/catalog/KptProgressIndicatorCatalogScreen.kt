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
import template.core.base.designsystem.component.KptCircularProgressIndicator
import template.core.base.designsystem.component.KptDownloadProgress
import template.core.base.designsystem.component.KptLinearProgressIndicator
import template.core.base.designsystem.component.KptLoadingDots
import template.core.base.designsystem.component.KptLoadingPulse
import template.core.base.designsystem.component.KptLoadingWave
import template.core.base.designsystem.component.KptProgressIndicator
import template.core.base.designsystem.component.KptProgressWithLabel
import template.core.base.designsystem.component.KptScaffold
import template.core.base.designsystem.component.KptTopAppBar
import template.core.base.designsystem.component.KptUploadProgress
import template.core.base.designsystem.core.ProgressIndicatorVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KptProgressIndicatorCatalogScreen(
    navigateBack: () -> Unit,
) {
    KptScaffold(
        topBar = {
            KptTopAppBar(
                title = "KptProgressIndicator Catalog",
                onNavigationIconClick = navigateBack,
            )
        },
    ) {
        var selectedDemo by remember { mutableStateOf<ProgressIndicatorDemoType?>(null) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "KptProgressIndicator Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            items(items = ProgressIndicatorDemoType.entries.toList()) { demoType ->
                ProgressIndicatorCatalogItem(
                    demoType = demoType,
                    onClick = { selectedDemo = demoType },
                )
            }
        }

        // Show the selected progress indicator demo
        when (selectedDemo) {
            ProgressIndicatorDemoType.CIRCULAR_INDETERMINATE -> CircularIndeterminateExample()
            ProgressIndicatorDemoType.CIRCULAR_DETERMINATE -> CircularDeterminateExample()
            ProgressIndicatorDemoType.LINEAR_INDETERMINATE -> LinearIndeterminateExample()
            ProgressIndicatorDemoType.LINEAR_DETERMINATE -> LinearDeterminateExample()
            ProgressIndicatorDemoType.DOTS -> DotsExample()
            ProgressIndicatorDemoType.WAVE -> WaveExample()
            ProgressIndicatorDemoType.PULSE -> PulseExample()
            ProgressIndicatorDemoType.RING -> RingExample()
            ProgressIndicatorDemoType.WITH_LABEL -> ProgressWithLabelExample()
            ProgressIndicatorDemoType.UPLOAD -> UploadProgressExample()
            ProgressIndicatorDemoType.DOWNLOAD -> DownloadProgressExample()
            null -> {}
        }
    }
}

@Composable
private fun ProgressIndicatorCatalogItem(
    demoType: ProgressIndicatorDemoType,
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

private enum class ProgressIndicatorDemoType(
    val title: String,
    val description: String,
) {
    CIRCULAR_INDETERMINATE("Circular Indeterminate", "KptCircularProgressIndicator (indeterminate)"),
    CIRCULAR_DETERMINATE("Circular Determinate", "KptCircularProgressIndicator with progress"),
    LINEAR_INDETERMINATE("Linear Indeterminate", "KptLinearProgressIndicator (indeterminate)"),
    LINEAR_DETERMINATE("Linear Determinate", "KptLinearProgressIndicator with progress"),
    DOTS("Loading Dots", "KptLoadingDots animated indicator"),
    WAVE("Wave", "KptLoadingWave animated indicator"),
    PULSE("Pulse", "KptLoadingPulse animated indicator"),
    RING("Ring", "KptProgressIndicator with ring animation"),
    WITH_LABEL("Progress With Label", "KptProgressWithLabel with progress and label"),
    UPLOAD("Upload Progress", "KptUploadProgress with file info and cancel"),
    DOWNLOAD("Download Progress", "KptDownloadProgress with speed and time"),
}

@Composable
private fun CircularIndeterminateExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptCircularProgressIndicator()
    }
}

@Composable
private fun CircularDeterminateExample() {
    var progress by remember { mutableStateOf(0.6f) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptCircularProgressIndicator(progress = progress, showProgressText = true)
    }
}

@Composable
private fun LinearIndeterminateExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptLinearProgressIndicator()
    }
}

@Composable
private fun LinearDeterminateExample() {
    var progress by remember { mutableStateOf(0.4f) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptLinearProgressIndicator(progress = progress)
    }
}

@Composable
private fun DotsExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptLoadingDots()
    }
}

@Composable
private fun WaveExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptLoadingWave()
    }
}

@Composable
private fun PulseExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptLoadingPulse()
    }
}

@Composable
private fun RingExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptProgressIndicator(variant = ProgressIndicatorVariant.Ring)
    }
}

@Composable
private fun ProgressWithLabelExample() {
    var progress by remember { mutableStateOf(0.7f) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptProgressWithLabel(progress = progress, label = "Loading Data")
    }
}

@Composable
private fun UploadProgressExample() {
    var progress by remember { mutableStateOf(0.5f) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptUploadProgress(
            progress = progress,
            fileName = "report.pdf",
            fileSize = "2.3 MB",
            onCancel = {},
        )
    }
}

@Composable
private fun DownloadProgressExample() {
    var progress by remember { mutableStateOf(0.8f) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptDownloadProgress(
            progress = progress,
            downloadSpeed = "1.2 MB/s",
            timeRemaining = "00:12 left",
        )
    }
}
