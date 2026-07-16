/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.component.HeroCard
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.spacing
import kpt.core.ui.scaffold.KptScaffold
import kpt.feature.profile.generated.resources.Res
import kpt.feature.profile.generated.resources.screens_profile_local_message
import kpt.feature.profile.generated.resources.screens_profile_local_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ProfileScreen(modifier: Modifier = Modifier) {
    ProfileScreenContent(
        modifier = modifier
            .fillMaxSize()
            .testTag(TestTags.Profile.SCREEN),
    )
}

@Composable
internal fun ProfileScreenContent(modifier: Modifier = Modifier) {
    val sp = MaterialTheme.spacing
    KptScaffold(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(sp.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            HeroCard {
                Column(
                    modifier = Modifier.padding(sp.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(sp.sm),
                ) {
                    Icon(
                        imageVector = AppIcons.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                    )
                    Text(
                        text = stringResource(Res.string.screens_profile_local_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(Res.string.screens_profile_local_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
