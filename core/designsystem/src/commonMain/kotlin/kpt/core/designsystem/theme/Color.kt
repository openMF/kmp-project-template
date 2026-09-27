/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// ── Light scheme ─────────────────────────────────────────────────────────────
// Primary — Trust Indigo (fintech-grade, more saturated than stock M3 indigo).
/**
 * Material `primary` role, light scheme — Trust Indigo. The app's main brand role — key actions and selected
 * states.
 */
val primaryLight = Color(0xFF4338CA)

/**
 * Content colour on `primaryLight` (light scheme) — text and icons drawn over it. Contrast-paired; do not
 * substitute another colour here.
 */
val onPrimaryLight = Color(0xFFFFFFFF)

/**
 * Material `primary container` role, light scheme — Trust Indigo. The app's main brand role — key actions and
 * selected states.
 */
val primaryContainerLight = Color(0xFFE0E7FF)

/**
 * Content colour on `primaryContainerLight` (light scheme) — text and icons drawn over it. Contrast-paired; do not
 * substitute another colour here.
 */
val onPrimaryContainerLight = Color(0xFF1F1D75)

// Secondary — Emerald (financial growth, positive deltas, primary CTAs).
/** Material `secondary` role, light scheme — Emerald. Supporting role — positive deltas and secondary CTAs. */
val secondaryLight = Color(0xFF059669)

/**
 * Content colour on `secondaryLight` (light scheme) — text and icons drawn over it. Contrast-paired; do not
 * substitute another colour here.
 */
val onSecondaryLight = Color(0xFFFFFFFF)

/**
 * Material `secondary container` role, light scheme — Emerald. Supporting role — positive deltas and secondary
 * CTAs.
 */
val secondaryContainerLight = Color(0xFFD1FAE5)

/**
 * Content colour on `secondaryContainerLight` (light scheme) — text and icons drawn over it. Contrast-paired; do
 * not substitute another colour here.
 */
val onSecondaryContainerLight = Color(0xFF064E3B)

// Tertiary — Warm Amber (highlights, badges, warnings-but-not-errors).
/** Material `tertiary` role, light scheme — Warm Amber. Accent role — highlights and badges; NOT an error signal. */
val tertiaryLight = Color(0xFFD97706)

/**
 * Content colour on `tertiaryLight` (light scheme) — text and icons drawn over it. Contrast-paired; do not
 * substitute another colour here.
 */
val onTertiaryLight = Color(0xFFFFFFFF)

/**
 * Material `tertiary container` role, light scheme — Warm Amber. Accent role — highlights and badges; NOT an error
 * signal.
 */
val tertiaryContainerLight = Color(0xFFFEF3C7)

/**
 * Content colour on `tertiaryContainerLight` (light scheme) — text and icons drawn over it. Contrast-paired; do
 * not substitute another colour here.
 */
val onTertiaryContainerLight = Color(0xFF78350F)

// Error — Light red-orange (warmer than saturated red; still reads as urgent).
/**
 * Material `error` role, light scheme — warm red-orange. Failure role — destructive actions and validation
 * failures.
 */
val errorLight = Color(0xFFF87171)

/**
 * Content colour on `errorLight` (light scheme) — text and icons drawn over it. Contrast-paired; do not substitute
 * another colour here.
 */
val onErrorLight = Color(0xFFFFFFFF)

/**
 * Material `error container` role, light scheme — warm red-orange. Failure role — destructive actions and
 * validation failures.
 */
val errorContainerLight = Color(0xFFFFE4E1)

/**
 * Content colour on `errorContainerLight` (light scheme) — text and icons drawn over it. Contrast-paired; do not
 * substitute another colour here.
 */
val onErrorContainerLight = Color(0xFF9F1239)

// Background + surface — warm-cool neutral hierarchy (M3 surfaceContainer ladder).
/** Material `background` role, light scheme. The window behind all content. */
val backgroundLight = Color(0xFFFAFAFB)

/**
 * Content colour on `backgroundLight` (light scheme) — text and icons drawn over it. Contrast-paired; do not
 * substitute another colour here.
 */
val onBackgroundLight = Color(0xFF0F172A)

/** Material `surface` role, light scheme. The base sheet components sit on. */
val surfaceLight = Color(0xFFFAFAFB)

/**
 * Content colour on `surfaceLight` (light scheme) — text and icons drawn over it. Contrast-paired; do not
 * substitute another colour here.
 */
val onSurfaceLight = Color(0xFF0F172A)

/** Material `surface variant` role, light scheme. The base sheet components sit on. */
val surfaceVariantLight = Color(0xFFE2E8F0)

/**
 * Content colour on `surfaceVariantLight` (light scheme) — text and icons drawn over it. Contrast-paired; do not
 * substitute another colour here.
 */
val onSurfaceVariantLight = Color(0xFF475569)

/** Material `outline` role, light scheme. Borders and dividers. */
val outlineLight = Color(0xFF94A3B8)

/** Material `outline variant` role, light scheme. Borders and dividers. */
val outlineVariantLight = Color(0xFFCBD5E1)

/** Material `scrim` role, light scheme. The dim behind a modal. */
val scrimLight = Color(0xFF000000)

/** Material `inverse surface` role, light scheme. Inverted pairing, for snackbars and tooltips over content. */
val inverseSurfaceLight = Color(0xFF1E293B)

/** Material `inverse on surface` role, light scheme. Inverted pairing, for snackbars and tooltips over content. */
val inverseOnSurfaceLight = Color(0xFFF1F5F9)

/** Material `inverse primary` role, light scheme. Inverted pairing, for snackbars and tooltips over content. */
val inversePrimaryLight = Color(0xFFA5B4FC)

// Surface tonal ladder — used by AppCard / HeroCard to feel lifted.
/** Material `surface dim` role, light scheme. The base sheet components sit on. */
val surfaceDimLight = Color(0xFFE2E8F0)

/** Material `surface bright` role, light scheme. The base sheet components sit on. */
val surfaceBrightLight = Color(0xFFFAFAFB)

/** Material `surface container lowest` role, light scheme. The base sheet components sit on. */
val surfaceContainerLowestLight = Color(0xFFFFFFFF)

/** Material `surface container low` role, light scheme. The base sheet components sit on. */
val surfaceContainerLowLight = Color(0xFFF8FAFC)

/** Material `surface container` role, light scheme. The base sheet components sit on. */
val surfaceContainerLight = Color(0xFFF1F5F9)

/** Material `surface container high` role, light scheme. The base sheet components sit on. */
val surfaceContainerHighLight = Color(0xFFE2E8F0)

/** Material `surface container highest` role, light scheme. The base sheet components sit on. */
val surfaceContainerHighestLight = Color(0xFFCBD5E1)

// ── Dark scheme ──────────────────────────────────────────────────────────────
/**
 * Material `primary` role, dark scheme — Trust Indigo. The app's main brand role — key actions and
 * selected states.
 */
val primaryDark = Color(0xFFA5B4FC)

/**
 * Content colour on `primaryDark` (dark scheme) — text and icons drawn over it. Contrast-paired; do not substitute
 * another colour here.
 */
val onPrimaryDark = Color(0xFF1F1D75)

/**
 * Material `primary container` role, dark scheme — Trust Indigo. The app's main brand role — key actions and
 * selected states.
 */
val primaryContainerDark = Color(0xFF3730A3)

/**
 * Content colour on `primaryContainerDark` (dark scheme) — text and icons drawn over it. Contrast-paired; do not
 * substitute another colour here.
 */
val onPrimaryContainerDark = Color(0xFFE0E7FF)

/** Material `secondary` role, dark scheme — Emerald. Supporting role — positive deltas and secondary CTAs. */
val secondaryDark = Color(0xFF6EE7B7)

/**
 * Content colour on `secondaryDark` (dark scheme) — text and icons drawn over it. Contrast-paired; do not
 * substitute another colour here.
 */
val onSecondaryDark = Color(0xFF064E3B)

/**
 * Material `secondary container` role, dark scheme — Emerald. Supporting role — positive deltas and
 * secondary CTAs.
 */
val secondaryContainerDark = Color(0xFF065F46)

/**
 * Content colour on `secondaryContainerDark` (dark scheme) — text and icons drawn over it. Contrast-paired; do not
 * substitute another colour here.
 */
val onSecondaryContainerDark = Color(0xFFD1FAE5)

/** Material `tertiary` role, dark scheme — Warm Amber. Accent role — highlights and badges; NOT an error signal. */
val tertiaryDark = Color(0xFFFCD34D)

/**
 * Content colour on `tertiaryDark` (dark scheme) — text and icons drawn over it. Contrast-paired; do not
 * substitute another colour here.
 */
val onTertiaryDark = Color(0xFF78350F)

/**
 * Material `tertiary container` role, dark scheme — Warm Amber. Accent role — highlights and badges; NOT an error
 * signal.
 */
val tertiaryContainerDark = Color(0xFF92400E)

/**
 * Content colour on `tertiaryContainerDark` (dark scheme) — text and icons drawn over it. Contrast-paired; do not
 * substitute another colour here.
 */
val onTertiaryContainerDark = Color(0xFFFEF3C7)

/**
 * Material `error` role, dark scheme — warm red-orange. Failure role — destructive actions and validation
 * failures.
 */
val errorDark = Color(0xFFFDBA74)

/**
 * Content colour on `errorDark` (dark scheme) — text and icons drawn over it. Contrast-paired; do not substitute
 * another colour here.
 */
val onErrorDark = Color(0xFF7C2D12)

/**
 * Material `error container` role, dark scheme — warm red-orange. Failure role — destructive actions and
 * validation failures.
 */
val errorContainerDark = Color(0xFFC2410C)

/**
 * Content colour on `errorContainerDark` (dark scheme) — text and icons drawn over it. Contrast-paired; do not
 * substitute another colour here.
 */
val onErrorContainerDark = Color(0xFFFFEDD5)

/** Material `background` role, dark scheme. The window behind all content. */
val backgroundDark = Color(0xFF0F172A)

/**
 * Content colour on `backgroundDark` (dark scheme) — text and icons drawn over it. Contrast-paired; do not
 * substitute another colour here.
 */
val onBackgroundDark = Color(0xFFF1F5F9)

/** Material `surface` role, dark scheme. The base sheet components sit on. */
val surfaceDark = Color(0xFF0F172A)

/**
 * Content colour on `surfaceDark` (dark scheme) — text and icons drawn over it. Contrast-paired; do not substitute
 * another colour here.
 */
val onSurfaceDark = Color(0xFFF1F5F9)

/** Material `surface variant` role, dark scheme. The base sheet components sit on. */
val surfaceVariantDark = Color(0xFF1E293B)

/**
 * Content colour on `surfaceVariantDark` (dark scheme) — text and icons drawn over it. Contrast-paired; do not
 * substitute another colour here.
 */
val onSurfaceVariantDark = Color(0xFFCBD5E1)

/** Material `outline` role, dark scheme. Borders and dividers. */
val outlineDark = Color(0xFF64748B)

/** Material `outline variant` role, dark scheme. Borders and dividers. */
val outlineVariantDark = Color(0xFF334155)

/** Material `scrim` role, dark scheme. The dim behind a modal. */
val scrimDark = Color(0xFF000000)

/** Material `inverse surface` role, dark scheme. Inverted pairing, for snackbars and tooltips over content. */
val inverseSurfaceDark = Color(0xFFF1F5F9)

/** Material `inverse on surface` role, dark scheme. Inverted pairing, for snackbars and tooltips over content. */
val inverseOnSurfaceDark = Color(0xFF1E293B)

/** Material `inverse primary` role, dark scheme. Inverted pairing, for snackbars and tooltips over content. */
val inversePrimaryDark = Color(0xFF4338CA)

/** Material `surface dim` role, dark scheme. The base sheet components sit on. */
val surfaceDimDark = Color(0xFF0F172A)

/** Material `surface bright` role, dark scheme. The base sheet components sit on. */
val surfaceBrightDark = Color(0xFF374558)

/** Material `surface container lowest` role, dark scheme. The base sheet components sit on. */
val surfaceContainerLowestDark = Color(0xFF020617)

/** Material `surface container low` role, dark scheme. The base sheet components sit on. */
val surfaceContainerLowDark = Color(0xFF1E293B)

/** Material `surface container` role, dark scheme. The base sheet components sit on. */
val surfaceContainerDark = Color(0xFF243044)

/** Material `surface container high` role, dark scheme. The base sheet components sit on. */
val surfaceContainerHighDark = Color(0xFF2D3B52)

/** Material `surface container highest` role, dark scheme. The base sheet components sit on. */
val surfaceContainerHighestDark = Color(0xFF374558)
