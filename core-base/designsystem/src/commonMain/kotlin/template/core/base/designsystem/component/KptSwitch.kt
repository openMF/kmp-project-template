package template.core.base.designsystem.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import template.core.base.designsystem.core.ComponentConfigurationScope
import template.core.base.designsystem.core.ComponentDsl
import template.core.base.designsystem.core.KptComponent
import template.core.base.designsystem.theme.KptTheme

@Composable
fun KptSwitch(configuration: SwitchConfiguration) {
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
            Column(modifier = Modifier.weight(1f)) {
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
            Switch(
                checked = configuration.checked,
                onCheckedChange = configuration.onCheckedChange,
                modifier = Modifier.testTag(configuration.testTag ?: "KptSwitch"),
                enabled = configuration.enabled,
                colors = configuration.colors ?: SwitchDefaults.colors(),
                interactionSource = configuration.interactionSource,
                thumbContent = configuration.thumbContent,
            )
        }
    } else {
        Switch(
            checked = configuration.checked,
            onCheckedChange = configuration.onCheckedChange,
            modifier = configuration.modifier.testTag(configuration.testTag ?: "KptSwitch"),
            enabled = configuration.enabled,
            colors = configuration.colors ?: SwitchDefaults.colors(),
            interactionSource = configuration.interactionSource,
            thumbContent = configuration.thumbContent,
        )
    }
}

fun kptSwitch(block: SwitchBuilder.() -> Unit): SwitchConfiguration {
    return SwitchBuilder().apply(block).build()
}

@Immutable
data class SwitchConfiguration(
    override val testTag: String? = null,
    override val contentDescription: String? = null,
    override val modifier: Modifier = Modifier,
    val checked: Boolean,
    val onCheckedChange: ((Boolean) -> Unit)?,
    val enabled: Boolean = true,
    val colors: SwitchColors? = null,
    val interactionSource: MutableInteractionSource? = null,
    val thumbContent: (@Composable () -> Unit)? = null,
    val label: String? = null,
    val description: String? = null,
) : KptComponent

@ComponentDsl
class SwitchBuilder : ComponentConfigurationScope {
    override var testTag: String? = null
    override var contentDescription: String? = null
    override var enabled: Boolean = true
    override var modifier: Modifier = Modifier

    var checked: Boolean = false
    var onCheckedChange: ((Boolean) -> Unit)? = null
    var colors: SwitchColors? = null
    var interactionSource: MutableInteractionSource? = null
    var thumbContent: (@Composable () -> Unit)? = null
    var label: String? = null
    var description: String? = null

    fun build(): SwitchConfiguration = SwitchConfiguration(
        testTag = testTag,
        contentDescription = contentDescription,
        modifier = modifier,
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
        thumbContent = thumbContent,
        label = label,
        description = description,
    )
}