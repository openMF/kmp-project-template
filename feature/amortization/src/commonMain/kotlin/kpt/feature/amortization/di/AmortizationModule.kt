/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.amortization.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import kpt.feature.amortization.ui.AmortizationScheduleViewModel

/**
 * Koin module for the amortization-schedule feature.
 *
 * Wired into the app graph by `cmp-navigation/.../KoinModules.kt`.
 * The `LoanRepository` binding is provided by `core/data/.../RepositoryModule.kt`.
 */
val AmortizationModule = module {
    viewModel { params ->
        AmortizationScheduleViewModel(repository = get(), loanId = params.get())
    }
}
