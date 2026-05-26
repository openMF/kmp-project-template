/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.loans.ui

import org.mifos.core.model.banking.LoanKind
import kotlin.math.roundToLong

/**
 * Human-readable label for a [LoanKind] — used by chips, dropdowns, and detail screens.
 *
 * Forks that need localization should swap this for a string-resources lookup; for the
 * template we keep it inline so the multi-formKey showcase has zero infra dependencies.
 */
internal fun loanKindLabel(kind: LoanKind): String = when (kind) {
    LoanKind.PERSONAL -> "Personal"
    LoanKind.MORTGAGE -> "Mortgage"
    LoanKind.AUTO -> "Auto"
    LoanKind.STUDENT -> "Student"
    LoanKind.BUSINESS -> "Business"
    LoanKind.OTHER -> "Other"
}

/**
 * Compact money formatting used across loan rows. Two decimals when fractional, none when
 * round to the nearest dollar so the UI feels intentional.
 *
 * Intentionally minimal — forks plugging in their own locale-aware formatting should
 * substitute via a CompositionLocal or DI in `core/store`.
 */
internal fun formatMoney(value: Double): String {
    if (value.isNaN() || value.isInfinite()) return "—"
    val rounded = (value * 100.0).roundToLong()
    val whole = rounded / 100L
    val cents = (rounded % 100L).let { if (it < 0) -it else it }
    val sign = if (rounded < 0L && whole == 0L) "-" else ""
    return if (cents == 0L) "$$sign$whole" else "$$sign$whole.${cents.toString().padStart(2, '0')}"
}
