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
import kpt.core.base.analytics.di.analyticsModule
import kpt.core.base.common.di.CommonModule
import kpt.core.base.platform.di.platformModule
import kpt.core.base.security.di.SecurityModule
import kpt.core.data.di.DataModule
import kpt.core.database.di.DatabaseModule
import kpt.core.datastore.di.DatastoreModule
import kpt.core.store.di.appStoreModule
import kpt.feature.amortization.di.AmortizationModule
import kpt.feature.bills.di.BillsModule
import kpt.feature.calculators.di.CalculatorsModule
import kpt.feature.crypto.di.CryptoFeatureModule
import kpt.feature.currencyrates.di.CurrencyRatesModule
import kpt.feature.emicalculator.di.EmiCalculatorModule
import kpt.feature.home.di.HomeModule
import kpt.feature.loans.di.LoansModule
import kpt.feature.macro.di.MacroModule
import kpt.feature.rates.di.RatesModule
import kpt.feature.settings.SettingsModule
import kpt.sync.di.SyncModule
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

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
            // shell (framework) — kept
            HomeModule,
            SettingsModule,
            // demo:begin — customizer --clean strips these demo feature modules
            CurrencyRatesModule,
            EmiCalculatorModule,
            BillsModule,
            LoansModule,
            AmortizationModule,
            RatesModule,
            CalculatorsModule,
            MacroModule,
            CryptoFeatureModule,
            // demo:end
        )
    }

    val allModules = listOf(
        SecurityModule,
        dataModule,
        DatabaseModule,
        dispatcherModule,
        analyticsModule,
        DatastoreModule,
        featureModule,
        AppModule,
        SyncModule,
    )
}
