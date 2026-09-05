/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.emicalculator.di

import kpt.core.domain.demo.emi.calculateEmi
import kpt.core.store.demo.emi.impl.EmiCompute
import kpt.feature.emicalculator.ui.EmiCalculatorViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val EmiCalculatorModule = module {
    // Binds the compute PORT declared by core/store. core/store cannot import core/domain
    // (store → domain → data → store would be a cycle), so the feature — which already sees
    // both — supplies the implementation and Koin joins them at runtime. See EmiStore.kt.
    single<EmiCompute> { EmiCompute { params -> calculateEmi(params.principal, params.ratePercent, params.tenureMonths) } }

    viewModelOf(::EmiCalculatorViewModel)
}
