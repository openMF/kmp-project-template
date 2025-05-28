/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.designsystem.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import template.core.base.designsystem.core.KptComponent
import template.core.base.designsystem.theme.KptTheme

/**
 * KptCheckbox is a customizable checkbox component that can optionally display a label and description.
 *
 * This composable wraps Material3's [Checkbox] and provides additional features such as a label, description,
 * and custom styling through [CheckboxConfiguration].
 *
 * If a label is provided, the checkbox and label are displayed in a row, with the label and optional description
 * shown in a column next to the checkbox. If no label is provided, only the checkbox is shown.
 *
 * @param configuration The [CheckboxConfiguration] object that defines the state, appearance, and behavior of the checkbox.
 */
@Composable
fun KptCheckbox(configuration: CheckboxConfiguration) {
    if (configuration.label != null) {
        Row(
            modifier = configuration.modifier
                .selectable(
                    selected = configuration.checked,
                    onClick = { configuration.onCheckedChange?.invoke(!configuration.checked) },
                    enabled = configuration.enabled,
                )
                .padding(horizontal = KptTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = configuration.checked,
                onCheckedChange = configuration.onCheckedChange,
                modifier = Modifier.testTag(configuration.testTag ?: "KptCheckbox"),
                enabled = configuration.enabled,
                colors = configuration.colors ?: CheckboxDefaults.colors(),
                interactionSource = configuration.interactionSource,
            )
            Spacer(modifier = Modifier.width(KptTheme.spacing.sm))
            Column {
                Text(
                    text = configuration.label,
                    style = KptTheme.typography.bodyLarge,
                )
                if (configuration.description != null) {
                    Text(
                        text = configuration.description,
                        style = KptTheme.typography.bodySmall,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    } else {
        Checkbox(
            checked = configuration.checked,
            onCheckedChange = configuration.onCheckedChange,
            modifier = configuration.modifier.testTag(configuration.testTag ?: "KptCheckbox"),
            enabled = configuration.enabled,
            colors = configuration.colors ?: CheckboxDefaults.colors(),
            interactionSource = configuration.interactionSource,
        )
    }
}

/**
 * Configuration for the [KptCheckbox] composable.
 *
 * @property checked Whether the checkbox is checked.
 * @property onCheckedChange Callback to be invoked when the checked state changes.
 * @property modifier Modifier to be applied to the checkbox or its container.
 * @property testTag Optional test tag for UI testing.
 * @property contentDescription Optional content description for accessibility.
 * @property enabled Whether the checkbox is enabled and can be interacted with.
 * @property colors Optional [CheckboxColors] to customize the checkbox appearance.
 * @property interactionSource Optional [MutableInteractionSource] for observing interaction events.
 * @property label Optional label text to display next to the checkbox.
 * @property description Optional description text to display below the label.
 */
@Immutable
data class CheckboxConfiguration(
    val checked: Boolean,
    val onCheckedChange: ((Boolean) -> Unit)?,
    override val modifier: Modifier = Modifier,
    override val testTag: String? = null,
    override val contentDescription: String? = null,
    val enabled: Boolean = true,
    val colors: CheckboxColors? = null,
    val interactionSource: MutableInteractionSource? = null,
    val label: String? = null,
    val description: String? = null,
) : KptComponent
