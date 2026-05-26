/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.loans.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.mifos.core.data.di.OutboxQualifiers
import org.mifos.feature.loans.ui.EditLoanViewModel
import org.mifos.feature.loans.ui.LoanDetailViewModel
import org.mifos.feature.loans.ui.PersonalLoansListViewModel

/**
 * Koin module for the personal-loans feature.
 *
 * Wired into the app graph by `cmp-navigation/.../KoinModules.kt`
 * (`featureModule.includes(LoansModule)`). The `SubmitOutbox<Loan>` and `LoanRepository`
 * bindings consumed here are provided by `core/data/.../RepositoryModule.kt`.
 */
val LoansModule = module {
    viewModel { PersonalLoansListViewModel(repository = get()) }
    viewModel { params ->
        LoanDetailViewModel(repository = get(), loanId = params.get())
    }
    viewModel { params ->
        EditLoanViewModel(
            repository = get(),
            outbox = get(qualifier = OutboxQualifiers.Loan),
            loanId = params.getOrNull(),
        )
    }
}
