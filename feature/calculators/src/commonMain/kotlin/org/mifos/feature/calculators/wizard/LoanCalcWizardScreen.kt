/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.calculators.wizard

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
import androidx.compose.material3.Card
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.mifos.core.common.formatGrouped
import org.mifos.core.model.banking.LoanCalcScenario

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
            title = { Text("Resume unfinished wizard?") },
            text = {
                Text(
                    "You have an in-progress loan-calc wizard at step ${candidate.currentStep}. " +
                        "Continue where you left off, or discard?",
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::resumeDraft) { Text("Continue") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::discardSavedDraft) { Text("Discard") }
            },
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            val lastStep = LoanCalcWizardViewModel.LAST_STEP
            val titleText = "New Loan · Step ${form.currentStep}/$lastStep"
            TopAppBar(
                title = { Text(titleText) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
        Text("How much do you want to borrow?", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = form.principal.toLong().toString(),
            onValueChange = { it.toDoubleOrNull()?.let(viewModel::onUpdatePrincipal) },
            label = { Text("Principal Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StepTenure(form: LoanCalcScenario, viewModel: LoanCalcWizardViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Over how many months?", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = form.tenureMonths.toString(),
            onValueChange = { it.toIntOrNull()?.let(viewModel::onUpdateTenure) },
            label = { Text("Tenure (months)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StepRate(form: LoanCalcScenario, viewModel: LoanCalcWizardViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("At what annual rate?", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = form.ratePercent.toString(),
            onValueChange = { it.toDoubleOrNull()?.let(viewModel::onUpdateRate) },
            label = { Text("Annual Rate (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StepReview(form: LoanCalcScenario, preview: org.mifos.core.model.emi.EmiResult) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("Review", style = MaterialTheme.typography.titleMedium)
            Text("Principal: ${form.principal.formatGrouped(2)}")
            Text("Tenure: ${form.tenureMonths} months")
            Text("Rate: ${form.ratePercent}%")
            Text("Monthly EMI: ${preview.emi.formatGrouped(2)}")
            Text("Total Interest: ${preview.totalInterest.formatGrouped(2)}")
            Text("Total Payable: ${preview.totalPayment.formatGrouped(2)}")
        }
    }
}

@Composable
private fun StepNameAndSave(form: LoanCalcScenario, viewModel: LoanCalcWizardViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Name this scenario so you can find it again in your loans.",
            style = MaterialTheme.typography.titleMedium,
        )
        OutlinedTextField(
            value = form.name,
            onValueChange = viewModel::onUpdateName,
            label = { Text("Scenario Name") },
            singleLine = true,
            isError = form.name.isBlank(),
            supportingText = {
                if (form.name.isBlank()) {
                    Text(
                        "Required — type a name to enable Save.",
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
        OutlinedButton(onClick = onBack, enabled = currentStep > 1) { Text("Back") }
        if (currentStep < LoanCalcWizardViewModel.LAST_STEP) {
            Button(onClick = onNext) { Text("Next") }
        } else {
            // Disable when validation fails so the user can see WHY the tap doesn't fire,
            // instead of completeAndSave() silently early-returning.
            Button(onClick = onComplete, enabled = canSave) { Text("Save as Loan") }
        }
    }
}
