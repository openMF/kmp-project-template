package org.mifos.feature.onboarding

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.feature.onboarding.components.OnBoardingScreenPage

const val Total_Pages = 3

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
                description = "This is onboarding",
                modifier=modifier.padding(it)
            )
            2 -> OnBoardingScreenPage(
                onNext = viewmodel::onNextPage,
                currentPage = currentPage,
                title = "Welcome",
                description = "This is onboarding",
                modifier=modifier.padding(it)
            )
            3 -> OnBoardingScreenPage(
                onNext = viewmodel::onNextPage,
                currentPage = currentPage,
                title = "Welcome",
                description = "This is onboarding",
                modifier=modifier.padding(it)
            )
        }
    }
}