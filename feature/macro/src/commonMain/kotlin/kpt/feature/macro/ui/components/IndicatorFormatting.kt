/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.macro.ui.components

import kpt.core.common.formatDecimal
import kpt.core.model.economic.IndicatorKind
import kpt.core.model.economic.MacroIndicator

/**
 * Localised display name for an [IndicatorKind] — what the IndicatorCard's
 * title text reads. Kept here next to its formatter so future labels can be
 * added without touching the screen code.
 */
fun IndicatorKind.displayName(): String = when (this) {
    IndicatorKind.GDP -> "GDP"
    IndicatorKind.INFLATION_CPI -> "Inflation (CPI)"
    IndicatorKind.UNEMPLOYMENT -> "Unemployment"
    IndicatorKind.GDP_PER_CAPITA -> "GDP per Capita"
    IndicatorKind.GINI -> "GINI Index"
}

/**
 * Best-effort headline value formatter.
 *
 * - GDP / GDP_PER_CAPITA → compact currency (`$25.46T`, `$1,234`)
 * - INFLATION_CPI / UNEMPLOYMENT / GINI → percentage with 1-2 decimals (`3.7%`)
 *
 * Returns `"—"` when no observation has a non-null value (World Bank has a
 * known pattern of returning empty / null for unreported series). Picking
 * the most recent non-null is the right behavior for an "at-a-glance"
 * dashboard card.
 */
fun MacroIndicator.headlineValue(): String {
    val latest = observations.asReversed().firstOrNull { it.value != null }?.value ?: return "—"
    return when (indicator) {
        IndicatorKind.GDP, IndicatorKind.GDP_PER_CAPITA -> formatCurrencyCompact(latest)
        IndicatorKind.INFLATION_CPI, IndicatorKind.UNEMPLOYMENT, IndicatorKind.GINI ->
            "${latest.formatDecimal(1)}%"
    }
}

/**
 * Year label for the headline value — used as the small subtitle under the
 * value (e.g. "Latest: 2023"). Returns null when no usable observation
 * exists.
 */
fun MacroIndicator.latestYear(): Int? = observations.asReversed().firstOrNull { it.value != null }?.year

private fun formatCurrencyCompact(value: Double): String {
    val abs = kotlin.math.abs(value)
    return when {
        abs >= 1_000_000_000_000.0 -> "\$${(value / 1_000_000_000_000.0).formatDecimal(2)}T"
        abs >= 1_000_000_000.0 -> "\$${(value / 1_000_000_000.0).formatDecimal(2)}B"
        abs >= 1_000_000.0 -> "\$${(value / 1_000_000.0).formatDecimal(2)}M"
        abs >= 1_000.0 -> "\$${(value / 1_000.0).formatDecimal(1)}K"
        else -> "\$${value.formatDecimal(2)}"
    }
}
