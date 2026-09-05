/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.demo.profile

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.demo.profile.ProfileInfo

/** Read surface for the profile screen (`static_content`, MEMORY_ONLY). */
interface ProfileRepository {

    /** A [ScreenDataStream] over the profile info. */
    fun profileStream(scope: CoroutineScope): ScreenDataStream<ProfileInfo>
}
