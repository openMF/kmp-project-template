package template.core.base.designsystem.component

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun KptSimpleListItem(
    text: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onClick: (() -> Unit)? = null
) {
    KptListItem(
        ListItemConfiguration(
            headlineContent = { Text(text) },
            supportingContent = supportingText?.let { { Text(it) } },
            leadingContent = leadingIcon?.let { { Icon(it, contentDescription = null) } },
            trailingContent = trailingIcon?.let { { Icon(it, contentDescription = null) } },
            onClick = onClick,
            modifier = modifier
        )
    )
}