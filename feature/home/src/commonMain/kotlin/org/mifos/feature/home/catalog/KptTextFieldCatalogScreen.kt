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
import template.core.base.designsystem.component.KptEmailField
import template.core.base.designsystem.component.KptMultiLineTextField
import template.core.base.designsystem.component.KptNumberField
import template.core.base.designsystem.component.KptPasswordField
import template.core.base.designsystem.component.KptPhoneField
import template.core.base.designsystem.component.KptScaffold
import template.core.base.designsystem.component.KptSearchField
import template.core.base.designsystem.component.KptTextField
import template.core.base.designsystem.component.KptTopAppBar
import template.core.base.designsystem.component.KptUrlField
import template.core.base.designsystem.core.TextFieldVariant
import template.core.base.designsystem.core.ValidationState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KptTextFieldCatalogScreen(
    navigateBack: () -> Unit,
) {
    KptScaffold(
        topBar = {
            KptTopAppBar(
                title = "KptTextField Catalog",
                onNavigationIconClick = navigateBack,
            )
        },
    ) {
        var selectedDemo by remember { mutableStateOf<TextFieldDemoType?>(null) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "KptTextField Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            items(items = TextFieldDemoType.entries.toList()) { demoType ->
                TextFieldCatalogItem(
                    demoType = demoType,
                    onClick = { selectedDemo = demoType },
                )
            }
        }

        // Show the selected text field demo
        when (selectedDemo) {
            TextFieldDemoType.BASIC -> BasicTextFieldExample()
            TextFieldDemoType.OUTLINED -> OutlinedTextFieldExample()
            TextFieldDemoType.FILLED -> FilledTextFieldExample()
            TextFieldDemoType.EMAIL_VALIDATION -> EmailFieldValidationExample()
            TextFieldDemoType.PASSWORD_TOGGLE -> PasswordFieldToggleExample()
            TextFieldDemoType.PHONE -> PhoneFieldExample()
            TextFieldDemoType.SEARCH -> SearchFieldExample()
            TextFieldDemoType.NUMBER -> NumberFieldExample()
            TextFieldDemoType.MULTILINE -> MultiLineFieldExample()
            TextFieldDemoType.URL -> UrlFieldExample()
            TextFieldDemoType.CHAR_COUNTER -> CharCounterFieldExample()
            null -> {}
        }
    }
}

@Composable
private fun TextFieldCatalogItem(
    demoType: TextFieldDemoType,
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

private enum class TextFieldDemoType(
    val title: String,
    val description: String,
) {
    BASIC("Basic Text Field", "A simple text field with label."),
    OUTLINED("Outlined Text Field", "Outlined variant of KptTextField."),
    FILLED("Filled Text Field", "Filled variant of KptTextField."),
    EMAIL_VALIDATION("Email Field with Validation", "KptEmailField with validation state."),
    PASSWORD_TOGGLE("Password Field with Toggle", "KptPasswordField with visibility toggle."),
    PHONE("Phone Field", "KptPhoneField for phone numbers."),
    SEARCH("Search Field", "KptSearchField with clear and search action."),
    NUMBER("Number Field", "KptNumberField for numeric input."),
    MULTILINE("Multi-line Field", "KptMultiLineTextField for longer input."),
    URL("URL Field", "KptUrlField for URLs with prefix and validation."),
    CHAR_COUNTER("Text Field with Char Counter", "KptTextField with character counter."),
}

@Composable
private fun BasicTextFieldExample() {
    var value by remember { mutableStateOf("") }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptTextField(
            value = value,
            onValueChange = { value = it },
            label = "Name",
        )
    }
}

@Composable
private fun OutlinedTextFieldExample() {
    var value by remember { mutableStateOf("") }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptTextField(
            value = value,
            onValueChange = { value = it },
            label = "Outlined",
            variant = TextFieldVariant.Outlined,
        )
    }
}

@Composable
private fun FilledTextFieldExample() {
    var value by remember { mutableStateOf("") }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptTextField(
            value = value,
            onValueChange = { value = it },
            label = "Filled",
            variant = TextFieldVariant.Filled,
        )
    }
}

@Composable
private fun EmailFieldValidationExample() {
    var value by remember { mutableStateOf("") }
    var validation by remember {
        mutableStateOf<ValidationState>(ValidationState.None)
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            KptEmailField(
                value = value,
                onValueChange = {
                    value = it
                    validation = when {
                        it.isEmpty() -> ValidationState.None
                        !it.contains("@") -> ValidationState.Invalid("Invalid email")
                        else -> ValidationState.Valid
                    }
                },
                validationState = validation,
            )
        }
    }
}

@Composable
private fun PasswordFieldToggleExample() {
    var value by remember { mutableStateOf("") }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptPasswordField(
            value = value,
            onValueChange = { value = it },
            showVisibilityToggle = true,
        )
    }
}

@Composable
private fun PhoneFieldExample() {
    var value by remember { mutableStateOf("") }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptPhoneField(
            value = value,
            onValueChange = { value = it },
        )
    }
}

@Composable
private fun SearchFieldExample() {
    var value by remember { mutableStateOf("") }
    var lastSearch by remember { mutableStateOf("") }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            KptSearchField(
                value = value,
                onValueChange = { value = it },
                onSearch = { lastSearch = it },
            )
            if (lastSearch.isNotEmpty()) {
                Text("Last search: $lastSearch", modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
private fun NumberFieldExample() {
    var value by remember { mutableStateOf("") }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptNumberField(
            value = value,
            onValueChange = { value = it },
        )
    }
}

@Composable
private fun MultiLineFieldExample() {
    var value by remember { mutableStateOf("") }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptMultiLineTextField(
            value = value,
            onValueChange = { value = it },
            minLines = 3,
            maxLines = 6,
        )
    }
}

@Composable
private fun UrlFieldExample() {
    var value by remember { mutableStateOf("") }
    var validation by remember { mutableStateOf<ValidationState>(ValidationState.None) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptUrlField(
            value = value,
            onValueChange = {
                value = it
                validation = when {
                    it.isEmpty() -> ValidationState.None
                    !it.startsWith("https://") -> ValidationState.Invalid("URL must start with https://")
                    else -> ValidationState.Valid
                }
            },
            validationState = validation,
        )
    }
}

@Composable
private fun CharCounterFieldExample() {
    var value by remember { mutableStateOf("") }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptTextField(
            value = value,
            onValueChange = { value = it },
            label = "With Char Counter",
            variant = TextFieldVariant.Outlined,
            enabled = true,
            modifier = Modifier,
        )
        // For demo, show a character counter below
        Text(
            text = "${value.length}/20",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.BottomCenter).padding(top = 8.dp),
        )
    }
}
