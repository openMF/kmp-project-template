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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.component.AppCard
import kpt.feature.calculators.TestTags
import kpt.core.base.designsystem.component.HeroCard
import kpt.core.common.formatGrouped
import kpt.core.designsystem.component.AmountDisplay
import kpt.core.designsystem.theme.spacing
import kpt.core.model.demo.banking.LoanCalcScenario
import kpt.feature.calculators.generated.resources.Res
import kpt.feature.calculators.generated.resources.screens_calc_wizard_back_cd
import kpt.feature.calculators.generated.resources.screens_calc_wizard_button_back
import kpt.feature.calculators.generated.resources.screens_calc_wizard_button_next
import kpt.feature.calculators.generated.resources.screens_calc_wizard_button_save
import kpt.feature.calculators.generated.resources.screens_calc_wizard_resume_continue
import kpt.feature.calculators.generated.resources.screens_calc_wizard_resume_discard
import kpt.feature.calculators.generated.resources.screens_calc_wizard_resume_message
import kpt.feature.calculators.generated.resources.screens_calc_wizard_resume_title
import kpt.feature.calculators.generated.resources.screens_calc_wizard_step_name_label
import kpt.feature.calculators.generated.resources.screens_calc_wizard_step_name_message
import kpt.feature.calculators.generated.resources.screens_calc_wizard_step_name_required
import kpt.feature.calculators.generated.resources.screens_calc_wizard_step_principal_label
import kpt.feature.calculators.generated.resources.screens_calc_wizard_step_principal_question
import kpt.feature.calculators.generated.resources.screens_calc_wizard_step_rate_label
import kpt.feature.calculators.generated.resources.screens_calc_wizard_step_rate_question
import kpt.feature.calculators.generated.resources.screens_calc_wizard_step_review_emi_label
import kpt.feature.calculators.generated.resources.screens_calc_wizard_step_review_principal_label
import kpt.feature.calculators.generated.resources.screens_calc_wizard_step_review_rate_label
import kpt.feature.calculators.generated.resources.screens_calc_wizard_step_review_rate_value
import kpt.feature.calculators.generated.resources.screens_calc_wizard_step_review_tenure_label
import kpt.feature.calculators.generated.resources.screens_calc_wizard_step_review_tenure_value
import kpt.feature.calculators.generated.resources.screens_calc_wizard_step_review_total_interest_label
import kpt.feature.calculators.generated.resources.screens_calc_wizard_step_review_total_supporting
import kpt.feature.calculators.generated.resources.screens_calc_wizard_step_tenure_label
import kpt.feature.calculators.generated.resources.screens_calc_wizard_step_tenure_question
import kpt.feature.calculators.generated.resources.screens_calc_wizard_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanCalcWizardScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    scenarioId: String? = null,
    viewModel: LoanCalcWizardViewModel = koinViewModel { parametersOf(scenarioId) },
) {
    val form by viewModel.formState.collectAsStateWithLifecycle()
    val preview by viewModel.preview.collectAsStateWithLifecycle()
    val resumeCandidate by viewModel.resumeCandidate.collectAsStateWithLifecycle()

    resumeCandidate?.let { candidate ->
        AlertDialog(
            onDismissRequest = viewModel::discardSavedDraft,
            title = { Text(stringResource(Res.string.screens_calc_wizard_resume_title)) },
            text = {
                Text(
                    stringResource(
                        Res.string.screens_calc_wizard_resume_message,
                        candidate.currentStep,
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::resumeDraft) {
                    Text(stringResource(Res.string.screens_calc_wizard_resume_continue))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::discardSavedDraft) {
                    Text(stringResource(Res.string.screens_calc_wizard_resume_discard))
                }
            },
        )
    }

    Scaffold(
        modifier = modifier.testTag(TestTags.Wizard.SCREEN),
        topBar = {
            val lastStep = LoanCalcWizardViewModel.LAST_STEP
            val titleText = stringResource(
                Res.string.screens_calc_wizard_title,
                form.currentStep,
                lastStep,
            )
            TopAppBar(
                title = { Text(titleText) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.screens_calc_wizard_back_cd),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LinearProgressIndicator(
                progress = { form.currentStep / LoanCalcWizardViewModel.LAST_STEP.toFloat() },
                modifier = Modifier.fillMaxWidth(),
            )

            Box(modifier = Modifier.weight(1f)) {
                when (form.currentStep) {
                    1 -> StepPrincipal(form, viewModel)
                    2 -> StepTenure(form, viewModel)
                    3 -> StepRate(form, viewModel)
                    4 -> StepReview(form, preview)
                    else -> StepNameAndSave(form, viewModel)
                }
            }

            WizardButtons(
                currentStep = form.currentStep,
                canSave = form.name.isNotBlank() &&
                    form.principal > 0.0 &&
                    form.ratePercent >= 0.0 &&
                    form.tenureMonths > 0,
                onBack = viewModel::goBack,
                onNext = viewModel::goNext,
                onComplete = viewModel::completeAndSave,
            )
        }
    }
}

@Composable
private fun StepPrincipal(form: LoanCalcScenario, viewModel: LoanCalcWizardViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(Res.string.screens_calc_wizard_step_principal_question),
            style = MaterialTheme.typography.titleMedium,
        )
        OutlinedTextField(
            value = form.principal.toLong().toString(),
            onValueChange = { it.toDoubleOrNull()?.let(viewModel::onUpdatePrincipal) },
            label = { Text(stringResource(Res.string.screens_calc_wizard_step_principal_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StepTenure(form: LoanCalcScenario, viewModel: LoanCalcWizardViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(Res.string.screens_calc_wizard_step_tenure_question),
            style = MaterialTheme.typography.titleMedium,
        )
        OutlinedTextField(
            value = form.tenureMonths.toString(),
            onValueChange = { it.toIntOrNull()?.let(viewModel::onUpdateTenure) },
            label = { Text(stringResource(Res.string.screens_calc_wizard_step_tenure_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StepRate(form: LoanCalcScenario, viewModel: LoanCalcWizardViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(Res.string.screens_calc_wizard_step_rate_question),
            style = MaterialTheme.typography.titleMedium,
        )
        OutlinedTextField(
            value = form.ratePercent.toString(),
            onValueChange = { it.toDoubleOrNull()?.let(viewModel::onUpdateRate) },
            label = { Text(stringResource(Res.string.screens_calc_wizard_step_rate_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StepReview(form: LoanCalcScenario, preview: kpt.core.model.demo.emi.EmiResult) {
    val sp = MaterialTheme.spacing
    Column(verticalArrangement = Arrangement.spacedBy(sp.md)) {
        HeroCard {
            AmountDisplay(
                amountText = preview.emi.formatGrouped(2),
                label = stringResource(Res.string.screens_calc_wizard_step_review_emi_label),
                supporting = {
                    Text(
                        text = stringResource(
                            Res.string.screens_calc_wizard_step_review_total_supporting,
                            preview.totalPayment.formatGrouped(2),
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
            )
        }
        AppCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(sp.md),
                verticalArrangement = Arrangement.spacedBy(sp.sm),
            ) {
                ReviewMetricRow(
                    label = stringResource(Res.string.screens_calc_wizard_step_review_principal_label),
                    value = form.principal.formatGrouped(2),
                )
                ReviewMetricRow(
                    label = stringResource(Res.string.screens_calc_wizard_step_review_tenure_label),
                    value = stringResource(
                        Res.string.screens_calc_wizard_step_review_tenure_value,
                        form.tenureMonths,
                    ),
                )
                ReviewMetricRow(
                    label = stringResource(Res.string.screens_calc_wizard_step_review_rate_label),
                    value = stringResource(
                        Res.string.screens_calc_wizard_step_review_rate_value,
                        form.ratePercent.toString(),
                    ),
                )
                ReviewMetricRow(
                    label = stringResource(Res.string.screens_calc_wizard_step_review_total_interest_label),
                    value = preview.totalInterest.formatGrouped(2),
                )
            }
        }
    }
}

@Composable
private fun ReviewMetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun StepNameAndSave(form: LoanCalcScenario, viewModel: LoanCalcWizardViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(Res.string.screens_calc_wizard_step_name_message),
            style = MaterialTheme.typography.titleMedium,
        )
        OutlinedTextField(
            value = form.name,
            onValueChange = viewModel::onUpdateName,
            label = { Text(stringResource(Res.string.screens_calc_wizard_step_name_label)) },
            singleLine = true,
            isError = form.name.isBlank(),
            supportingText = {
                if (form.name.isBlank()) {
                    Text(
                        text = stringResource(Res.string.screens_calc_wizard_step_name_required),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun WizardButtons(
    currentStep: Int,
    canSave: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onComplete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        OutlinedButton(onClick = onBack, enabled = currentStep > 1) {
            Text(stringResource(Res.string.screens_calc_wizard_button_back))
        }
        if (currentStep < LoanCalcWizardViewModel.LAST_STEP) {
            Button(onClick = onNext) {
                Text(stringResource(Res.string.screens_calc_wizard_button_next))
            }
        } else {
            // Disable when validation fails so the user can see WHY the tap doesn't fire,
            // instead of completeAndSave() silently early-returning.
            Button(onClick = onComplete, enabled = canSave) {
                Text(stringResource(Res.string.screens_calc_wizard_button_save))
            }
        }
    }
}
