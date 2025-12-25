package org.mifos.feature.onboarding

import androidx.compose.material3.Text
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import template.core.base.ui.composableWithStayTransitions


@Serializable
data object OnBoardingRoute

fun NavController.navigateToOnBoarding(navOptions: NavOptions? = null) = navigate(OnBoardingRoute, navOptions)

fun NavGraphBuilder.onboardingDestination() {
    composableWithStayTransitions<OnBoardingRoute> {
        OnboardingScreen()
    }
}