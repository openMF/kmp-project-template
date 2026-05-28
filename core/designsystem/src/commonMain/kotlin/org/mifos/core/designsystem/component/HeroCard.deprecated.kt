/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:Suppress("unused")

package org.mifos.core.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.mifos.core.designsystem.theme.spacing
import template.core.base.designsystem.component.HeroCard as KptHeroCard

/**
 * Back-compat shim — `HeroCard` was promoted to `core-base/designsystem`.
 *
 * Forward to [template.core.base.designsystem.component.HeroCard]; this overload is kept
 * only so existing imports of `org.mifos.core.designsystem.component.HeroCard` keep
 * compiling for one release. New code should import from `core-base/designsystem`.
 *
 * `@Composable` functions cannot be `typealias`'d, so this is a forwarding shim rather
 * than a type alias.
 *
 * Note: this shim resolves `contentPadding` using the legacy mifos
 * `MaterialTheme.spacing.xl` (24dp) at the call site so behaviour is byte-identical to
 * pre-promotion code. The promoted core-base default uses `KptTheme.spacing.lg` which is
 * also 24dp — same visual result, different token name.
 */
@Deprecated(
    "Promoted to core-base/designsystem; import template.core.base.designsystem.component.HeroCard",
    ReplaceWith(
        "HeroCard(modifier, contentPadding, gradientStart, gradientEnd, content)",
        "template.core.base.designsystem.component.HeroCard",
    ),
    level = DeprecationLevel.WARNING,
)
@Composable
fun HeroCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(MaterialTheme.spacing.xl),
    gradientStart: Color = MaterialTheme.colorScheme.primary,
    gradientEnd: Color = MaterialTheme.colorScheme.primaryContainer,
    content: @Composable () -> Unit,
) {
    KptHeroCard(
        modifier = modifier,
        contentPadding = contentPadding,
        gradientStart = gradientStart,
        gradientEnd = gradientEnd,
        content = content,
    )
}
