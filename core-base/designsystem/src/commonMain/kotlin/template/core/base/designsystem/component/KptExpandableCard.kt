package template.core.base.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun KptExpandableCard(
    title: String,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {},
    icon: ImageVector? = null,
    subtitle: String? = null,
    expandedContent: @Composable () -> Unit
) {
    Card(
        modifier = modifier.testTag("KptExpandableCard")
    ) {
        Column {
            ListItem(
                headlineContent = { Text(title) },
                supportingContent = subtitle?.let { { Text(it) } },
                leadingContent = icon?.let { { Icon(it, contentDescription = null) } },
                trailingContent = {
                    IconButton(onClick = { onExpandedChange(!expanded) }) {
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Collapse" else "Expand"
                        )
                    }
                }
            )

            androidx.compose.animation.AnimatedVisibility(
                visible = expanded,
                enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    expandedContent()
                }
            }
        }
    }
}