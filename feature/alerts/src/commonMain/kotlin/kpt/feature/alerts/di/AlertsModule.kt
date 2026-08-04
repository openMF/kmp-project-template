/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.alerts.di

import kpt.core.data.di.OutboxQualifiers
import kpt.feature.alerts.ui.AlertCreateViewModel
import kpt.feature.alerts.ui.AlertsListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for the Alerts feature (`submit_offline_write` demo).
 *
 * - [AlertsListViewModel] reads the reactive [kpt.core.data.demo.alerts.AlertsRepository].
 * - [AlertCreateViewModel] injects the DI-qualified `SubmitOutbox<PriceAlert>`
 *   (`OutboxQualifiers.PriceAlert`) so its draft handler persists to the shared
 *   `framework_submit_drafts` outbox — the offline-first write seam.
 */
val AlertsModule = module {
    viewModel { AlertsListViewModel(repository = get()) }
    viewModel {
        AlertCreateViewModel(
            repository = get(),
            outbox = get(qualifier = OutboxQualifiers.PriceAlert),
        )
    }
}
