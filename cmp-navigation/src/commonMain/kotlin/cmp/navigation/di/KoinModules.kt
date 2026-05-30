/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.di

import cmp.navigation.AppViewModel
import cmp.navigation.authenticatednavbar.AuthenticatedNavbarNavigationViewModel
import cmp.navigation.rootnav.RootNavViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.mifos.core.data.di.DataModule
import org.mifos.core.database.di.DatabaseModule
import org.mifos.core.datastore.di.DatastoreModule
import org.mifos.core.store.di.appStoreModule
import org.mifos.sync.di.SyncModule
import org.mifos.feature.bills.di.BillsModule
import org.mifos.feature.calculators.di.CalculatorsModule
import org.mifos.feature.currencyrates.di.CurrencyRatesModule
import org.mifos.feature.emicalculator.di.EmiCalculatorModule
import org.mifos.feature.home.di.HomeModule
import org.mifos.feature.loans.di.LoansModule
import org.mifos.feature.macro.di.MacroModule
import org.mifos.feature.rates.di.RatesModule
import org.mifos.feature.settings.SettingsModule
import template.core.base.analytics.di.analyticsModule
import template.core.base.common.di.CommonModule
import template.core.base.platform.di.platformModule
import template.core.base.security.di.SecurityModule

// Archived 2026-05-24 (Money Toolkit pivot) — restore by re-importing + re-including in
// `featureModule` per feature/_archive/{module}/README.md:
//   import org.mifos.feature.alerts.di.AlertsModule
//   import org.mifos.feature.crypto.di.CryptoModule
//   import org.mifos.feature.watchlist.di.WatchlistModule

object KoinModules {
    private val dataModule = module {
        includes(DataModule, appStoreModule)
    }

    private val dispatcherModule = module {
        includes(CommonModule)
    }

    private val AppModule = module {
        includes(platformModule)

        viewModelOf(::AppViewModel)
        viewModelOf(::AuthenticatedNavbarNavigationViewModel)
        viewModelOf(::RootNavViewModel)
    }

    private val featureModule = module {
        includes(
            CurrencyRatesModule,
            EmiCalculatorModule,
            HomeModule,
            SettingsModule,
            BillsModule,
            LoansModule,
            RatesModule,
            CalculatorsModule,
            MacroModule,
        )
    }

    private val syncModule = SyncModule

    val allModules = listOf(
        SecurityModule,
        dataModule,
        DatabaseModule,
        dispatcherModule,
        analyticsModule,
        DatastoreModule,
        featureModule,
        AppModule,
        syncModule,
    )
}
