/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.demo.banking

import kotlinx.serialization.Serializable

/**
 * Wizard payload — the serialized snapshot of the loan-calc wizard at the
 * current step. Serializable so the offline-resilient `DraftSubmitHandler` can
 * persist it across process death and the next session can resume in-place.
 *
 * The wizard advances Amount → Tenure → Rate → Review → Save. [currentStep]
 * is the 1-based step the user was on when the snapshot was taken.
 *
 * Lives in `core/model/banking/` (not `feature/calculators/`) so the outbox
 * binding in `core/data/.../RepositoryModule.kt` can reference it without
 * pulling a layer-violating feature → core dep.
 *
 * @property scenarioId Stable identifier for this in-progress wizard run. New
 *   wizards generate a UUID-ish id; resumed wizards re-use the prior id.
 * @property name User-supplied label for the scenario (filled at Review).
 * @property principal Loan amount entered at step 1.
 * @property ratePercent APR entered at step 3.
 * @property tenureMonths Tenure entered at step 2.
 * @property currentStep 1..5 — used to restore the UI to its last step on resume.
 */
@Serializable
data class LoanCalcScenario(
    val scenarioId: String,
    val name: String = "",
    val principal: Double = 0.0,
    val ratePercent: Double = 0.0,
    val tenureMonths: Int = 0,
    val currentStep: Int = 1,
)
