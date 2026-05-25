/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.alerts.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.mifos.core.data.di.OutboxQualifiers
import org.mifos.feature.alerts.ui.PriceAlertsListViewModel
import org.mifos.feature.alerts.ui.SetPriceAlertViewModel

val AlertsModule = module {
    viewModel { PriceAlertsListViewModel(get()) }
    viewModel {
        SetPriceAlertViewModel(
            repository = get(),
            outbox = get(qualifier = OutboxQualifiers.PriceAlert),
        )
    }
}
