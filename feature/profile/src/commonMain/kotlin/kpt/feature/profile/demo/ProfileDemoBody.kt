/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.profile.demo

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.component.HeroCard
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.model.demo.profile.ProfileInfo
import kpt.feature.profile.demo.ui.ProfileViewModel
import org.koin.compose.viewmodel.koinNavViewModel as retainedKoinViewModel
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.spacing
import kpt.feature.profile.generated.resources.Res
import kpt.feature.profile.generated.resources.screens_profile_local_message
import kpt.feature.profile.generated.resources.screens_profile_local_title
import org.jetbrains.compose.resources.stringResource

/**
 * The profile tab's fork-owned INNER content (the default demo body). `ProfileScreen` (the
 * template-owned shell) renders this opaque body inside its framework-owned [kpt.core.ui.scaffold.KptScaffold];
 * the fork owns what renders here via `cmp-navigation`'s `BackboneRegistry.profileBody`, mirroring the
 * shipped `homeBody` → `HomeDashboard` seam. A `customizer --clean` fork strips the `BackboneRegistry`
 * fenced block, leaving an empty body for the fork to fill — the shell chrome survives unchanged.
 */
@Composable
fun ProfileDemoBody(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = retainedKoinViewModel(),
) {
    // Store5 read surface: the body consumes the repository-built STREAM directly, the same way
    // feature/watchlist and feature/macro's detail screen do — `ScreenContent(stream = …)` collects
    // with lifecycle awareness and wires `onRetry = stream::retry` itself, so there is no
    // collectAsStateWithLifecycle here and no re-exposed StateFlow on the ViewModel. Today the
    // store serves a local placeholder, so it settles on Content immediately — but a fork that
    // swaps the fetcher for a signed-in user gets Loading / Error / retry for free instead of
    // restructuring this composable.
    ScreenContent(
        stream = viewModel.profile,
        modifier = modifier,
    ) { profile, _ ->
        ProfileDemoContent(profile = profile)
    }
}

@Composable
private fun ProfileDemoContent(
    profile: ProfileInfo,
    modifier: Modifier = Modifier,
) {
    val sp = MaterialTheme.spacing
    Column(
        modifier = modifier
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
                    // App display name templated in from the common AppInfo accessor, never hardcoded.
                    text = stringResource(Res.string.screens_profile_local_message, profile.appDisplayName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
