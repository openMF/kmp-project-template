/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.calculators.wizard

import androidx.compose.runtime.Composable
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.demo.banking.LoanCalcScenario
import kpt.core.model.demo.emi.EmiResult
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier (SCREENSHOT_TEST.md CMP-PRIMARY).
 * `CommonComposablePreviewScanner` auto-discovers these from commonMain and renders them off
 * `desktopTest` — no emulator, no Robolectric.
 *
 * `LoanCalcWizardScreen` is not previewed: it resolves its ViewModel through Koin. Each wizard STEP
 * is, because the steps now take a single typed callback instead of the whole ViewModel — the same
 * stateless shape every other helper in this codebase uses, which is what makes them renderable
 * here at all.
 *
 * Literals are PREVIEW FIXTURE DATA — never reachable from the running app.
 */

private fun previewForm(
    name: String = "Car loan", // i18n:skip
    currentStep: Int = 1,
) = LoanCalcScenario(
    scenarioId = "scenario-1", // i18n:skip
    name = name,
    principal = 100_000.0,
    ratePercent = 7.5,
    tenureMonths = 60,
    currentStep = currentStep,
)

@Preview
@Composable
internal fun StepPrincipalPreview() {
    KptTheme {
        StepPrincipal(form = previewForm(), onPrincipalChange = {})
    }
}

@Preview
@Composable
internal fun StepTenurePreview() {
    KptTheme {
        StepTenure(form = previewForm(currentStep = 2), onTenureChange = {})
    }
}

@Preview
@Composable
internal fun StepRatePreview() {
    KptTheme {
        StepRate(form = previewForm(currentStep = 3), onRateChange = {})
    }
}

@Preview
@Composable
internal fun StepReviewPreview() {
    KptTheme {
        StepReview(
            form = previewForm(currentStep = 4),
            preview = EmiResult(emi = 2_003.79, totalPayment = 120_227.4, totalInterest = 20_227.4),
        )
    }
}

@Preview
@Composable
internal fun ReviewMetricRowPreview() {
    KptTheme {
        ReviewMetricRow(label = "Monthly EMI", value = "$2,003.79") // i18n:skip
    }
}

@Preview
@Composable
internal fun StepNameAndSavePreview() {
    KptTheme {
        StepNameAndSave(form = previewForm(currentStep = 5), onNameChange = {})
    }
}

@Preview
@Composable
internal fun StepNameAndSaveBlankNamePreview() {
    // A blank name is the wizard's one validation error — it drives the error tint plus the
    // supporting-text line, and it is what gates the Save button on the final step.
    KptTheme {
        StepNameAndSave(form = previewForm(name = "", currentStep = 5), onNameChange = {})
    }
}

@Preview
@Composable
internal fun WizardButtonsMidWizardPreview() {
    KptTheme {
        WizardButtons(
            currentStep = 2,
            canSave = false,
            onBack = {},
            onNext = {},
            onComplete = {},
        )
    }
}

@Preview
@Composable
internal fun WizardButtonsFinalStepPreview() {
    // The last step swaps Next for Complete, and `canSave` decides whether it is enabled. Both
    // differences are invisible in the mid-wizard render above.
    KptTheme {
        WizardButtons(
            currentStep = 5,
            canSave = true,
            onBack = {},
            onNext = {},
            onComplete = {},
        )
    }
}
