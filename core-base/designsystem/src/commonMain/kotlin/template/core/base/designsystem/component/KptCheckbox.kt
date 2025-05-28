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