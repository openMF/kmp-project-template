/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.calculators.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.mifos.feature.calculators.affordability.AffordabilityCalculatorViewModel
import org.mifos.feature.calculators.amortization.AmortizationViewModel
import org.mifos.feature.calculators.comparison.LoanComparisonViewModel
import org.mifos.feature.calculators.wizard.LoanCalcWizardViewModel

/**
 * DI module for the four calculator ViewModels.
 *
 * - [AffordabilityCalculatorViewModel] / [LoanComparisonViewModel] — zero-dep
 *   pure-compute VMs.
 * - [AmortizationViewModel] — takes an optional `loanId: String?` parameter so
 *   the screen can pre-fill from a saved loan. Wired via Koin `parametersOf`.
 * - [LoanCalcWizardViewModel] — uses the same `parametersOf` pattern for the
 *   `scenarioId: String?` argument, plus `SubmitOutbox<LoanCalcScenario>` +
 *   `LoanRepository` from app DI.
 */
val CalculatorsModule = module {
    viewModelOf(::AffordabilityCalculatorViewModel)
    viewModelOf(::LoanComparisonViewModel)
    viewModel { (loanId: String?) ->
        AmortizationViewModel(repository = get(), loanId = loanId)
    }
    viewModel { (scenarioId: String?) ->
        LoanCalcWizardViewModel(
            outbox = get(),
            repository = get(),
            scenarioIdArg = scenarioId,
        )
    }
}
