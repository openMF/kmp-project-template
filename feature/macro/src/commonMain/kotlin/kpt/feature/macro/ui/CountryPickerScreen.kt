/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.macro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.model.demo.economic.Country
import kpt.feature.macro.generated.resources.Res
import kpt.feature.macro.generated.resources.screens_macro_back_cd
import kpt.feature.macro.generated.resources.screens_macro_picker_search_placeholder
import kpt.feature.macro.generated.resources.screens_macro_picker_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Country picker — search + flag-emoji list. Selecting a row reports the
 * chosen [Country.code] back via [onCountryPicked]; the host screen owns the
 * "navigate back, ChangeCountry action" wiring.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryPickerScreen(
    onBackClick: () -> Unit,
    onCountryPicked: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CountryPickerViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.screens_macro_picker_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.screens_macro_back_cd),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.trySendAction(CountryPickerAction.Search(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(Res.string.screens_macro_picker_search_placeholder)) },
                singleLine = true,
            )
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.results, key = { it.code }) { country ->
                    CountryRow(country, onClick = { onCountryPicked(country.code) })
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun CountryRow(country: Country, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ISO-code badge replaces the regional-indicator flag emoji
        // (🇺🇸 / 🇮🇳 / …). Those code points are not in the project's
        // Outfit font and render as tofu on wasmJs (Skia does not fall
        // back to system fonts for missing glyphs). The badge is a
        // theme-tinted circle with the 2-letter ISO code — visually
        // consistent, asset-free, works for every country.
        Box(
            modifier = Modifier
                .padding(end = 16.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = country.code,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(country.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                country.code,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
