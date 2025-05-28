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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import template.core.base.designsystem.config.KptTestTags
import template.core.base.designsystem.core.EnhancedFabVariant
import template.core.base.designsystem.core.FabState
import template.core.base.designsystem.core.KptFloatingActionButtonConfiguration
import template.core.base.designsystem.theme.KptTheme

// Main Enhanced Component
@Composable
fun KptFloatingActionButton(configuration: KptFloatingActionButtonConfiguration) {
    val finalModifier = configuration.modifier
        .testTag(configuration.testTag ?: KptTestTags.FLOATING_ACTION_BUTTON)
        .let { mod ->
            if (configuration.contentDescription != null) {
                mod.semantics { contentDescription = configuration.contentDescription }
            } else {
                mod
            }
        }

    val finalContainerColor = when (configuration.state) {
        FabState.Normal -> configuration.containerColor ?: KptTheme.colorScheme.primary
        FabState.Loading -> configuration.containerColor ?: KptTheme.colorScheme.primaryContainer.copy(
            alpha = 0.5f,
        )
        FabState.Success -> KptTheme.colorScheme.primaryContainer
        FabState.Error -> KptTheme.colorScheme.errorContainer
    }

    val finalIcon = when (configuration.state) {
        FabState.Normal -> configuration.icon ?: Icons.Default.Add
        FabState.Loading -> null // Will show loading indicator
        FabState.Success -> Icons.Default.Check
        FabState.Error -> Icons.Default.Error
    }

    when (configuration.variant) {
        EnhancedFabVariant.Small -> SmallFloatingActionButton(
            onClick = configuration.onClick,
            modifier = finalModifier,
            containerColor = finalContainerColor,
            contentColor = contentColorFor(finalContainerColor),
            elevation = configuration.elevation ?: FloatingActionButtonDefaults.elevation(),
            shape = configuration.shape ?: FloatingActionButtonDefaults.smallShape,
            interactionSource = configuration.interactionSource,
        ) {
            FabContent(configuration, finalIcon)
        }

        EnhancedFabVariant.Regular -> FloatingActionButton(
            onClick = configuration.onClick,
            modifier = finalModifier,
            containerColor = finalContainerColor,
            contentColor = contentColorFor(finalContainerColor),
            elevation = configuration.elevation ?: FloatingActionButtonDefaults.elevation(),
            shape = configuration.shape ?: FloatingActionButtonDefaults.shape,
            interactionSource = configuration.interactionSource,
        ) {
            FabContent(configuration, finalIcon)
        }

        EnhancedFabVariant.Large -> LargeFloatingActionButton(
            onClick = configuration.onClick,
            modifier = finalModifier,
            containerColor = finalContainerColor,
            contentColor = contentColorFor(finalContainerColor),
            elevation = configuration.elevation ?: FloatingActionButtonDefaults.elevation(),
            shape = configuration.shape ?: FloatingActionButtonDefaults.largeShape,
            interactionSource = configuration.interactionSource,
        ) {
            FabContent(configuration, finalIcon)
        }

        EnhancedFabVariant.Extended -> ExtendedFloatingActionButton(
            onClick = configuration.onClick,
            icon = {
                if (finalIcon != null) {
                    @androidx.compose.runtime.Composable { FabContent(configuration, finalIcon) }
                } else {
                    null
                }
            },
            text = { Text(configuration.text ?: "Action") },
            modifier = finalModifier,
            expanded = configuration.expanded,
            containerColor = finalContainerColor,
            contentColor = contentColorFor(finalContainerColor),
            elevation = configuration.elevation ?: FloatingActionButtonDefaults.elevation(),
            shape = configuration.shape ?: FloatingActionButtonDefaults.extendedFabShape,
            interactionSource = configuration.interactionSource,
        )
    }
}

@Composable
private fun FabContent(
    configuration: KptFloatingActionButtonConfiguration,
    icon: ImageVector?,
) {
    when (configuration.state) {
        FabState.Loading -> {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = configuration.contentColor ?: ProgressIndicatorDefaults.circularColor,
            )
        }

        else -> {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = configuration.contentDescription,
                )
            }
        }
    }
}

// 1. Simple FAB with icon
@Composable
fun KptFloatingActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    variant: EnhancedFabVariant = EnhancedFabVariant.Regular,
    contentDescription: String? = null,
) {
    KptFloatingActionButton(
        KptFloatingActionButtonConfiguration(
            onClick = onClick,
            icon = icon,
            modifier = modifier,
            variant = variant,
            contentDescription = contentDescription,
        ),
    )
}

// 2. Extended FAB with text
@Composable
fun KptExtendedFloatingActionButton(
    onClick: () -> Unit,
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
) {
    KptFloatingActionButton(
        KptFloatingActionButtonConfiguration(
            onClick = onClick,
            variant = EnhancedFabVariant.Extended,
            icon = icon,
            text = text,
            expanded = expanded,
            modifier = modifier,
        ),
    )
}

// 3. Loading FAB
@Composable
fun KptLoadingFloatingActionButton(
    onClick: () -> Unit,
    loading: Boolean,
    icon: ImageVector = Icons.Default.Add,
    modifier: Modifier = Modifier,
    variant: EnhancedFabVariant = EnhancedFabVariant.Regular,
) {
    KptFloatingActionButton(
        KptFloatingActionButtonConfiguration(
            onClick = onClick,
            icon = icon,
            state = if (loading) FabState.Loading else FabState.Normal,
            modifier = modifier,
            variant = variant,
        ),
    )
}

// 4. Success FAB
@Composable
fun KptSuccessFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: EnhancedFabVariant = EnhancedFabVariant.Regular,
) {
    KptFloatingActionButton(
        KptFloatingActionButtonConfiguration(
            onClick = onClick,
            state = FabState.Success,
            modifier = modifier,
            variant = variant,
        ),
    )
}

// 5. Error FAB
@Composable
fun KptErrorFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: EnhancedFabVariant = EnhancedFabVariant.Regular,
) {
    KptFloatingActionButton(
        KptFloatingActionButtonConfiguration(
            onClick = onClick,
            state = FabState.Error,
            modifier = modifier,
            variant = variant,
        ),
    )
}

// 6. Small FAB
@Composable
fun KptSmallFloatingActionButton(
    onClick: () -> Unit,
    icon: ImageVector = Icons.Default.Add,
    modifier: Modifier = Modifier,
) {
    KptFloatingActionButton(
        onClick = onClick,
        icon = icon,
        modifier = modifier,
        variant = EnhancedFabVariant.Small,
    )
}

// 7. Large FAB
@Composable
fun KptLargeFloatingActionButton(
    onClick: () -> Unit,
    icon: ImageVector = Icons.Default.Add,
    modifier: Modifier = Modifier,
) {
    KptFloatingActionButton(
        onClick = onClick,
        icon = icon,
        modifier = modifier,
        variant = EnhancedFabVariant.Large,
    )
}

// 8. Action-specific FABs
@Composable
fun KptAddFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: EnhancedFabVariant = EnhancedFabVariant.Regular,
) {
    KptFloatingActionButton(
        onClick = onClick,
        icon = Icons.Default.Add,
        modifier = modifier,
        variant = variant,
        contentDescription = "Add",
    )
}

@Composable
fun KptEditFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: EnhancedFabVariant = EnhancedFabVariant.Regular,
) {
    KptFloatingActionButton(
        onClick = onClick,
        icon = Icons.Default.Edit,
        modifier = modifier,
        variant = variant,
        contentDescription = "Edit",
    )
}

@Composable
fun KptDeleteFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: EnhancedFabVariant = EnhancedFabVariant.Regular,
) {
    KptFloatingActionButton(
        KptFloatingActionButtonConfiguration(
            onClick = onClick,
            icon = Icons.Default.Delete,
            modifier = modifier,
            variant = variant,
            containerColor = MaterialTheme.colorScheme.error,
            contentDescription = "Delete",
        ),
    )
}

@Composable
fun KptShareFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: EnhancedFabVariant = EnhancedFabVariant.Regular,
) {
    KptFloatingActionButton(
        onClick = onClick,
        icon = Icons.Default.Share,
        modifier = modifier,
        variant = variant,
        contentDescription = "Share",
    )
}

@Composable
fun KptFavoriteFloatingActionButton(
    onClick: () -> Unit,
    isFavorite: Boolean,
    modifier: Modifier = Modifier,
    variant: EnhancedFabVariant = EnhancedFabVariant.Regular,
) {
    KptFloatingActionButton(
        KptFloatingActionButtonConfiguration(
            onClick = onClick,
            icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            modifier = modifier,
            variant = variant,
            containerColor = if (isFavorite) Color(0xFFF44336) else FloatingActionButtonDefaults.containerColor,
            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
        ),
    )
}

// 9. Multi-action FAB
@Composable
fun KptMultiActionFloatingActionButton(
    expanded: Boolean,
    onMainClick: () -> Unit,
    onExpandToggle: () -> Unit,
    actions: List<FabAction>,
    modifier: Modifier = Modifier,
    mainIcon: ImageVector = Icons.Default.Add,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = androidx.compose.ui.Alignment.End,
    ) {
        // Sub-actions
        androidx.compose.animation.AnimatedVisibility(
            visible = expanded,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically(),
        ) {
            Column(
                horizontalAlignment = androidx.compose.ui.Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                actions.forEach { action ->
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        ) {
                            Text(
                                text = action.label,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        KptSmallFloatingActionButton(
                            onClick = action.onClick,
                            icon = action.icon,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Main FAB
        KptFloatingActionButton(
            KptFloatingActionButtonConfiguration(
                onClick = {
                    if (expanded) onMainClick() else onExpandToggle()
                },
                icon = if (expanded) Icons.Default.Close else mainIcon,
                modifier = Modifier.testTag("MainFab"),
            ),
        )
    }
}

data class FabAction(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
)

// 10. Original component compatibility
@Composable
fun KptFloatingActionButton(
    variant: EnhancedFabVariant,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
    shape: Shape = FloatingActionButtonDefaults.shape,
    interactionSource: MutableInteractionSource? = null,
    testTag: String? = null,
    onClick: () -> Unit,
) {
    KptFloatingActionButton(
        KptFloatingActionButtonConfiguration(
            onClick = onClick,
            variant = variant,
            icon = icon,
            modifier = modifier,
            containerColor = containerColor,
            contentColor = contentColor,
            elevation = elevation,
            shape = shape,
            interactionSource = interactionSource,
            testTag = testTag,
        ),
    )
}
