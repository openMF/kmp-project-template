package template.core.base.designsystem.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import template.core.base.designsystem.core.KptComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KptListItem(configuration: ListItemConfiguration) {
    if (configuration.onClick != null) {
        ListItem(
            headlineContent = configuration.headlineContent,
            modifier = configuration.modifier
                .testTag(configuration.testTag ?: "KptListItem"),
            overlineContent = configuration.overlineContent,
            supportingContent = configuration.supportingContent,
            leadingContent = configuration.leadingContent,
            trailingContent = configuration.trailingContent,
            colors = configuration.colors ?: ListItemDefaults.colors(),
            tonalElevation = configuration.tonalElevation,
            shadowElevation = configuration.shadowElevation
        )
    } else {
        ListItem(
            headlineContent = configuration.headlineContent,
            modifier = configuration.modifier
                .testTag(configuration.testTag ?: "KptListItem"),
            overlineContent = configuration.overlineContent,
            supportingContent = configuration.supportingContent,
            leadingContent = configuration.leadingContent,
            trailingContent = configuration.trailingContent,
            colors = configuration.colors ?: ListItemDefaults.colors(),
            tonalElevation = configuration.tonalElevation,
            shadowElevation = configuration.shadowElevation
        )
    }
}

@Immutable
data class ListItemConfiguration(
    override val testTag: String? = null,
    override val contentDescription: String? = null,
    override val modifier: Modifier = Modifier,
    val headlineContent: @Composable () -> Unit,
    val supportingContent: (@Composable () -> Unit)? = null,
    val leadingContent: (@Composable () -> Unit)? = null,
    val trailingContent: (@Composable () -> Unit)? = null,
    val overlineContent: (@Composable () -> Unit)? = null,
    val colors: ListItemColors? = null,
    val tonalElevation: androidx.compose.ui.unit.Dp = 0.dp,
    val shadowElevation: androidx.compose.ui.unit.Dp = 0.dp,
    val onClick: (() -> Unit)? = null,
) : KptComponent
