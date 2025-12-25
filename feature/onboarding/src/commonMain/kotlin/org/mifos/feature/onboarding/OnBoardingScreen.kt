/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.onboarding

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.feature.onboarding.components.OnBoardingScreenPage

const val Total_Pages = 2

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    viewmodel: OnboardingViewmodel = koinViewModel(),
) {
    val currentPage by viewmodel.currentPage.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        when (currentPage) {
            1 -> OnBoardingScreenPage(
                onNext = viewmodel::onNextPage,
                currentPage = currentPage,
                title = "Welcome",
                description = "Thank you for using our template for creating your project",
                modifier=modifier.padding(it)
            )
            2 -> OnBoardingScreenPage(
                onNext = viewmodel::onNextPage,
                currentPage = currentPage,
                title = "Get Started",
                description = "You can now start using your project",
                modifier=modifier.padding(it)
            )
        }
    }
}