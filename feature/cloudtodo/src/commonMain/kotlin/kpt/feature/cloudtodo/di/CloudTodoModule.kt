/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.cloudtodo.di

import kpt.feature.cloudtodo.ui.CloudTodoViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for the Cloud Todo feature — the MUTABLE store-archetype write-path demo.
 *
 * A single ViewModel over `CloudTodoRepository`, which is already bound by
 * `core/data`'s ProjectRepositoryModule (it wires the read Store, the MutableStore and the
 * MutationGateway). Nothing extra is needed here: the point of this feature is that a screen can
 * drive the whole write path through the repository contract alone.
 */
val CloudTodoModule = module {
    viewModel { CloudTodoViewModel(repository = get()) }
}
