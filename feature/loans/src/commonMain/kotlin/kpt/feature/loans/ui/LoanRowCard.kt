/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loans.ui

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.component.AppCard
import kpt.core.designsystem.component.MoneyText
import kpt.core.designsystem.component.StatusChip
import kpt.core.designsystem.component.StatusChipIntent
import kpt.core.designsystem.theme.spacing
import kpt.core.model.banking.Loan
import kpt.core.model.banking.LoanKind
import kpt.feature.loans.generated.resources.Res
import kpt.feature.loans.generated.resources.screens_loans_row_monthly_emi_label
import kpt.feature.loans.generated.resources.screens_loans_row_next_due_value
import kpt.feature.loans.generated.resources.screens_loans_row_progress_value
import kpt.feature.loans.generated.resources.screens_loans_row_remaining_label
import org.jetbrains.compose.resources.stringResource

/**
 * Tappable row representing a single [Loan] in the list screen.
 *
 * Short-tap → navigate to detail (via [onClick]). Long-press → fire [onLongPress], which
 * the list screen wires to a delete-confirmation dialog.
 */
@Composable
internal fun LoanRowCard(
    loan: Loan,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sp = MaterialTheme.spacing
    val progress = if (loan.principal > 0) {
        (1.0 - (loan.principalRemaining / loan.principal)).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    AppCard(
        modifier = modifier.combinedClickable(onClick = onClick, onLongClick = onLongPress),
        accentColor = loanKindAccent(loan.kind),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = loan.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1f),
            )
            StatusChip(text = loanKindLabel(loan.kind), intent = StatusChipIntent.Info)
        }
        Spacer(Modifier.size(sp.sm))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.screens_loans_row_remaining_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                MoneyText(
                    text = formatMoney(loan.principalRemaining),
                    tone = kpt.core.designsystem.component.MoneyTone.Negative,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(Res.string.screens_loans_row_monthly_emi_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatMoney(loan.monthlyPayment),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }

        Spacer(Modifier.size(sp.md))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
        Spacer(Modifier.size(sp.xs))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(
                    Res.string.screens_loans_row_progress_value,
                    (progress * 100).toInt(),
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(
                    Res.string.screens_loans_row_next_due_value,
                    loan.nextDueDate.toString(),
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Loan-kind → accent stripe colour. Mirrored in the Home carousel
 * (see `feature/home/HomeScreen.kt#loanKindAccent`) so the visual vocabulary
 * stays consistent across the dashboard and the full loans list.
 */
@Composable
private fun loanKindAccent(kind: LoanKind): Color? = when (kind) {
    LoanKind.MORTGAGE -> MaterialTheme.colorScheme.secondary
    LoanKind.BUSINESS -> MaterialTheme.colorScheme.secondary
    LoanKind.AUTO -> MaterialTheme.colorScheme.tertiary
    LoanKind.STUDENT -> MaterialTheme.colorScheme.tertiary
    LoanKind.PERSONAL -> MaterialTheme.colorScheme.primary
    LoanKind.OTHER -> null
}
