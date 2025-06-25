/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.home.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.mifos.feature.home.StorageService
import org.mifos.feature.home.StorageServiceImpl
import org.mifos.feature.home.task.EditTaskViewModel
import org.mifos.feature.home.tasks.TasksViewModel

val HomeModule = module {
    single<StorageService> { StorageServiceImpl() }
    viewModelOf(::TasksViewModel)
    viewModelOf(::EditTaskViewModel)
}
