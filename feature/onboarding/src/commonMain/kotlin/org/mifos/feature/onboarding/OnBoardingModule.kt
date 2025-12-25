package org.mifos.feature.onboarding

import org.koin.dsl.module
import org.koin.core.module.dsl.viewModelOf

val OnboardingModule = module {
    viewModelOf(::OnboardingViewmodel)
}