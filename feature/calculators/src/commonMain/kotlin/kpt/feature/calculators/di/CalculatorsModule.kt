/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.calculators.di

import kpt.core.data.di.OutboxQualifiers
import kpt.feature.calculators.affordability.AffordabilityCalculatorViewModel
import kpt.feature.calculators.amortizationcalc.AmortizationViewModel
import kpt.feature.calculators.comparison.LoanComparisonViewModel
import kpt.feature.calculators.wizard.LoanCalcWizardViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

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
    // NOTE: `SubmitOutbox<LoanCalcScenario>` is registered in
    // `core/data/.../RepositoryModule.kt` alongside the other outboxes. The DAO type
    // lives in `core/database`, which feature modules deliberately do not depend on.
    // Without that binding, Koin's generic dispatch falls back to whichever
    // `SubmitOutbox<*>` was registered last in the app graph — at runtime that wired
    // the wrong serializer onto the wizard's outbox and produced
    // `ClassCastException: LoanCalcScenario cannot be cast to PriceAlert` on first
    // save. Regression captured by [LoanCalcScenarioSerializerTest].

    viewModelOf(::AffordabilityCalculatorViewModel)
    viewModelOf(::LoanComparisonViewModel)
    viewModel { (loanId: String?) ->
        AmortizationViewModel(repository = get(), loanId = loanId)
    }
    viewModel { (scenarioId: String?) ->
        LoanCalcWizardViewModel(
            outbox = get(qualifier = OutboxQualifiers.LoanCalcScenario),
            repository = get(),
            scenarioIdArg = scenarioId,
        )
    }
}
