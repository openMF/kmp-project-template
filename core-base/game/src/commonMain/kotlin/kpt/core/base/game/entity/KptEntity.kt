/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.game.entity

import androidx.compose.ui.geometry.Offset
import kotlin.math.hypot

/**
 * A minimal mutable 2D game entity: a position, an optional [lit] toggle (e.g. a lamp), and a
 * decaying [react] value (0..1) that game code uses to drive squash/pop feedback on tap/effect.
 *
 * Open so a game can subclass with sprite/behaviour fields; the engine only needs position +
 * hit-test + reaction decay.
 */
open class KptEntity(
    var x: Float,
    var y: Float,
    var lit: Boolean = false,
    var react: Float = 0f,
) {
    /** True when [p] is within [radius] of this entity's centre. */
    fun hitTest(p: Offset, radius: Float): Boolean = hypot(p.x - x, p.y - y) < radius

    /** Trigger a full reaction (call on tap / cause-and-effect). */
    fun poke() { react = 1f }

    /** Decay the reaction toward 0. Call once per frame with the frame delta. */
    fun decayReactions(dt: Float, rate: Float = 2.5f) {
        if (react > 0f) react = (react - dt * rate).coerceAtLeast(0f)
    }
}
