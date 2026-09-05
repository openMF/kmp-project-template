/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.profile.di

import kpt.core.base.ui.AppInfo
import kpt.core.model.demo.profile.ProfileInfo
import kpt.core.store.demo.profile.impl.ProfileInfoSource
import kpt.feature.profile.demo.ui.ProfileViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val ProfileModule = module {
    // Binds the read PORT declared by core/store. `AppInfo` lives in core-base/ui, which
    // core/store does not depend on, so the feature supplies the source. A fork that shows a
    // signed-in user replaces THIS binding — the ViewModel and Composable do not change.
    single<ProfileInfoSource> {
        ProfileInfoSource { ProfileInfo(appDisplayName = AppInfo.appDisplayName) }
    }

    viewModelOf(::ProfileViewModel)
}
