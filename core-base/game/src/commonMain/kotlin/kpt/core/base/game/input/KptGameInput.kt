/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.game.input

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Attach tap + drag handlers to a game surface (the reusable "controls" primitive).
 * - [onTap] fires on a discrete tap with the tap position.
 * - [onDragStart] / [onDrag] / [onDragEnd] drive drag-to-move; [onDrag] reports both the running
 *   pointer position and the per-move delta so games can move an object OR the character.
 */
fun Modifier.kptGameInput(
    onTap: (Offset) -> Unit = {},
    onDragStart: (Offset) -> Unit = {},
    onDrag: (position: Offset, delta: Offset) -> Unit = { _, _ -> },
    onDragEnd: () -> Unit = {},
): Modifier = this
    .pointerInput(Unit) { detectTapGestures(onTap = onTap) }
    .pointerInput(Unit) {
        var pos = Offset.Zero
        detectDragGestures(
            onDragStart = { pos = it; onDragStart(it) },
            onDrag = { change, delta -> change.consume(); pos += delta; onDrag(pos, delta) },
            onDragEnd = { onDragEnd() },
            onDragCancel = { onDragEnd() },
        )
    }
