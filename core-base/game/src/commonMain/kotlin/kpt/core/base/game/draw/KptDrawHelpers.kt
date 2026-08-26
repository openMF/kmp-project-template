/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.game.draw

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.sin

private const val TWO_PI = 6.2831855f

/** Rounded-rect convenience over [DrawScope] (x/y/w/h + uniform corner radius). */
fun DrawScope.drawRoundRectCompat(color: Color, x: Float, y: Float, w: Float, h: Float, radius: Float) {
    drawRoundRect(
        color = color,
        topLeft = Offset(x, y),
        size = Size(w, h),
        cornerRadius = CornerRadius(radius, radius),
    )
}

/** Linear interpolation between [a] and [b] by [t] in 0..1. */
fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

/** Idle-bob vertical offset: a sine wave at [hz] cycles/sec with amplitude [amp]. */
fun bob(tSeconds: Float, hz: Float = 0.45f, amp: Float = 6f): Float = sin(tSeconds * hz * TWO_PI) * amp

/** Squash-and-stretch pop scale from a 0..1 reaction value. */
fun popScale(react: Float, k: Float = 0.15f): Float =
    1f + if (react > 0f) sin(react * (TWO_PI / 2f)) * k else 0f
