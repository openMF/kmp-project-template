/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.designsystem.component.freshness

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.mifos.core.designsystem.component.FreshnessState
import template.core.base.ui.motion.kptRefreshingPulse

/**
 * Wraps a button/IconButton with an M3 [BadgedBox] that renders a 6.dp colored badge
 * when [state] ≠ [FreshnessState.Fresh]. The badge pulses while
 * [FreshnessState.Updating].
 *
 * Useful for refresh actions, sync buttons, or any control whose target data has a
 * non-trivial freshness status.
 *
 * Usage:
 * ```kotlin
 * ButtonFreshness(state = uiState.freshness) {
 *     IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, null) }
 * }
 * ```
 *
 * @param state Current freshness state. When [FreshnessState.Fresh], no badge renders.
 * @param modifier Modifier passed to the surrounding [BadgedBox].
 * @param content The button/IconButton to wrap.
 */
@Composable
fun ButtonFreshness(
    state: FreshnessState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    BadgedBox(
        modifier = modifier,
        badge = {
            if (state != FreshnessState.Fresh) {
                val color = resolveFreshnessColor(state)
                Badge(containerColor = color) {
                    // Empty badge — we just want the colored dot. M3 Badge with no
                    // content renders as a compact 6.dp circle.
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(color)
                            .kptRefreshingPulse(active = state == FreshnessState.Updating),
                    )
                }
            }
        },
        content = { content() },
    )
}
