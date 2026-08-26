/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.game.loop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos

/**
 * A frame-clock 2D game loop. Returns a frame-counter [MutableState] that ticks once per
 * rendered frame — READ it inside a `Canvas { }` draw lambda to drive continuous redraw.
 *
 * [onFrame] runs every frame with a clamped delta-time (seconds since last frame, capped at
 * [maxDeltaSeconds] to avoid huge steps after a pause) and the absolute time in seconds.
 *
 * Runs on any Compose Multiplatform target (Android / iOS / desktop / web).
 */
@Composable
fun rememberGameLoop(
    maxDeltaSeconds: Float = 0.05f,
    onFrame: (dt: Float, t: Float) -> Unit,
): MutableState<Long> {
    val frame = remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                val dt = if (last == 0L) 0.016f
                else ((now - last) / 1_000_000_000f).coerceAtMost(maxDeltaSeconds)
                last = now
                onFrame(dt, now / 1_000_000_000f)
                frame.value = frame.value + 1
            }
        }
    }
    return frame
}
