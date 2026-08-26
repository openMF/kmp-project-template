/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.game.particle

import kotlin.math.cos
import kotlin.math.sin

/** A single short-lived particle. [life] counts down to 0; [maxLife] lets renderers fade by ratio. */
class KptParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    val maxLife: Float = life,
)

/**
 * A tiny particle pool for cause-and-effect juice: integrate + gravity + decay + cull, plus a
 * radial [emitBurst]. Reduced-motion-aware games simply skip calling [emitBurst].
 */
class KptParticleSystem(private val gravity: Float = 0.25f) {

    val particles: MutableList<KptParticle> = mutableListOf()

    /** Spray [count] particles outward from ([x], [y]) in a ring. */
    fun emitBurst(x: Float, y: Float, count: Int, speed: Float = 4f, life: Float = 0.7f) {
        for (i in 0 until count) {
            val a = i.toFloat() / count * (2f * PI_F)
            particles.add(KptParticle(x, y, cos(a) * speed, sin(a) * speed - 2f, life))
        }
    }

    /** Advance all particles one frame and cull the dead. [frameScale] normalises velocity to ~60fps. */
    fun update(dt: Float, frameScale: Float = 60f) {
        val it = particles.iterator()
        while (it.hasNext()) {
            val p = it.next()
            p.x += p.vx * dt * frameScale
            p.y += p.vy * dt * frameScale
            p.vy += gravity
            p.life -= dt
            if (p.life <= 0f) it.remove()
        }
    }

    companion object {
        private const val PI_F = 3.1415927f
    }
}
