/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.game.surface

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.DrawScope
import kpt.core.base.game.loop.rememberGameLoop

/**
 * The reusable play surface: a Compose [Canvas] hosting a [rememberGameLoop]. Each frame [update]
 * advances the game state (dt, absolute-seconds), then [draw] renders the current frame — the
 * loop's frame counter is read inside the Canvas lambda so it redraws every frame.
 *
 * A game composes this with its own state + `Modifier.kptGameInput` for controls; the specific
 * scene/sprite drawing lives in the game (per-game), the loop + redraw plumbing lives here.
 */
@Composable
fun KptGameSurface(
    modifier: Modifier = Modifier,
    update: (dt: Float, t: Float) -> Unit,
    draw: DrawScope.() -> Unit,
) {
    val frame = rememberGameLoop(onFrame = update)
    Canvas(modifier = modifier) {
        @Suppress("UNUSED_EXPRESSION")
        frame.value // read → recompose/redraw each frame tick
        draw()
    }
}
