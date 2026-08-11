/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.demo.di

import kpt.core.base.security.FieldEncryptor
import kpt.core.database.AppDatabase
import kpt.core.database.demo.currency.converter.ChargeTypeConverters
import org.koin.dsl.module

/**
 * Marker singleton — its instantiation has the side effect of wiring [FieldEncryptor]
 * into [ChargeTypeConverters] before any database access. Bound with `createdAtStart = true`
 * so the install runs eagerly at Koin start, ahead of the first [AppDatabase] resolution.
 *
 * Room 3 KMP instantiates `@ColumnTypeConverters` classes via no-arg constructor, so the
 * encryptor cannot be passed in by constructor — it's injected post-construction through
 * the [ChargeTypeConverters.install] static method.
 */
private object ChargeTypeConvertersInstalled

/**
 * DemoDatabaseModule — the FORK-OWNED demo DAO/converter wiring for the toolkit showcase.
 *
 * Relocated out of the infra aggregator [kpt.core.database.di.DatabaseModule] (E1 / C3, epic
 * pure-white-label-store5-network) so that aggregator becomes an infra-only full-copy `owner:
 * template` file that carries ZERO `kpt.core.*.demo.*` imports — eliminating the sync-fragility
 * defect class (a template sync blind-overwriting the aggregator no longer re-introduces demo
 * imports a fork already stripped).
 *
 * Ownership: the `demo/` package is fork-owned in customization-surface.yaml. Installed into the app Koin graph
 * via the fork-owned `cmp-navigation/registry/FeatureRegistry.featureKoinModules` demo block; the
 * customizer `--clean` deletes this whole `demo/` package + empties that registry block together.
 */
val DemoDatabaseModule = module {
    single(createdAtStart = true) {
        ChargeTypeConverters.install(get<FieldEncryptor>())
        ChargeTypeConvertersInstalled
    }
    single { get<AppDatabase>().exchangeRatesDao }
    single { get<AppDatabase>().cloudTodoDao }
    single { get<AppDatabase>().coinMarketDao }
    single { get<AppDatabase>().coinDetailDao }
    single { get<AppDatabase>().rateHistoryDao }
    single { get<AppDatabase>().loanDao }
    single { get<AppDatabase>().billReminderDao }
    single { get<AppDatabase>().alertDao }
    single { get<AppDatabase>().interestRateSeriesDao }
}
