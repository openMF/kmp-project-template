/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.demo.di

import kpt.core.base.store.submit.OfflineSubmitSyncer
import kpt.core.base.store.submit.SubmitOutbox
import kpt.core.data.demo.alerts.AlertsRepository
import kpt.core.data.demo.alerts.impl.AlertsRepositoryImpl
import kpt.core.data.demo.banking.BillReminderRepository
import kpt.core.data.demo.banking.LoanRepository
import kpt.core.data.demo.banking.impl.BillReminderRepositoryImpl
import kpt.core.data.demo.banking.impl.LoanRepositoryImpl
import kpt.core.data.demo.cloudtodo.CloudTodoRepository
import kpt.core.data.demo.cloudtodo.impl.CloudTodoRepositoryImpl
import kpt.core.data.demo.crypto.CryptoRepository
import kpt.core.data.demo.crypto.impl.CryptoRepositoryImpl
import kpt.core.data.demo.currency.CurrencyRepository
import kpt.core.data.demo.currency.impl.CurrencyRepositoryImpl
import kpt.core.data.demo.economic.EconomicRatesRepository
import kpt.core.data.demo.economic.MacroIndicatorsRepository
import kpt.core.data.demo.economic.impl.EconomicRatesRepositoryImpl
import kpt.core.data.demo.economic.impl.MacroIndicatorsRepositoryImpl
import kpt.core.data.demo.watchlist.WatchlistRepository
import kpt.core.data.demo.watchlist.impl.WatchlistRepositoryImpl
import kpt.core.data.di.OutboxQualifiers
import kpt.core.data.infra.NetworkMonitor
import kpt.core.data.infra.impl.RoomBookkeeper
import kpt.core.data.infra.impl.RoomSubmitOutbox
import kpt.core.database.AppDatabase
import kpt.core.model.demo.alerts.PriceAlert
import kpt.core.model.demo.banking.BillReminder
import kpt.core.model.demo.banking.Loan
import kpt.core.model.demo.banking.LoanCalcScenario
import kpt.core.store.AppStoreRegistry
import kpt.core.store.demo.cloudtodo.impl.CloudTodoKey
import org.koin.dsl.module
import org.mobilenativefoundation.store.store5.Bookkeeper

/**
 * DemoRepositoryModule — the FORK-OWNED demo repository/outbox/syncer wiring for the toolkit showcase.
 *
 * Relocated out of the infra aggregator [kpt.core.data.di.DataModule] (in `RepositoryModule.kt`)
 * (E1 / C1, epic pure-white-label-store5-network) so that aggregator becomes an infra-only full-copy
 * `owner: template` file carrying ZERO `kpt.core.*.demo.*` imports — eliminating the sync-fragility
 * defect class (a template sync blind-overwriting the aggregator no longer re-introduces demo imports
 * a fork already stripped).
 *
 * Dependency resolution is lazy: every `networkMonitor = get()` / `fetchedAtRepository = get()` /
 * `scope = get()` resolves the framework-infra singletons still declared in [kpt.core.data.di.DataModule];
 * every `get(AppStoreRegistry.X)` resolves the Store from `core/store` appStoreModule; the demo DAOs come
 * from [kpt.core.database.demo.di.DemoDatabaseModule]. All of those modules are co-loaded with THIS one
 * whenever the demo showcase is present.
 *
 * Ownership: the `demo/` package is fork-owned in customization-surface.yaml. Installed into the app Koin graph via the
 * fork-owned `cmp-navigation/registry/FeatureRegistry.featureKoinModules` demo block; the customizer
 * `--clean` deletes this whole `demo/` package + empties that registry block together.
 */
val DemoRepositoryModule = module {
    // Personal watchlist — local-only persistence for the SubmitHandler showcase.
    single { get<AppDatabase>().watchlistDao }
    single<WatchlistRepository> {
        WatchlistRepositoryImpl(
            watchlistStore = get(AppStoreRegistry.Watchlist),
            dao = get(),
            networkMonitor = get(),
            fetchedAtRepository = get(),
        )
    }

    // Banking domain — purely local Loan + Bill Reminder persistence.
    // No remote sync; the DraftSubmitHandler outboxes below give the UX
    // polish (saving badge, retry on failure) for a local commit "submit".
    single { get<AppDatabase>().loanDao }
    single { get<AppDatabase>().billReminderDao }
    single<LoanRepository> {
        LoanRepositoryImpl(
            loansStore = get(AppStoreRegistry.Loans),
            loanDao = get(),
            networkMonitor = get(),
            fetchedAtRepository = get(),
        )
    }
    single<BillReminderRepository> {
        BillReminderRepositoryImpl(
            billRemindersStore = get(AppStoreRegistry.BillReminders),
            billReminderDao = get(),
            networkMonitor = get(),
            fetchedAtRepository = get(),
        )
    }

    // Outboxes — each form payload type gets its own RoomSubmitOutbox so
    // formKey collisions across features are impossible. The "submit" target
    // for both is the local repository's `upsert`, simulating remote sync.
    // All four SubmitOutbox bindings MUST declare their qualifier — Koin matches
    // single<> definitions by raw type (SubmitOutbox::class), not full KType, so
    // multiple SubmitOutbox<*> bindings collide and the last one wins regardless of
    // the generic parameter. See `OutboxQualifiers` KDoc for full background.
    single<SubmitOutbox<Loan>>(qualifier = OutboxQualifiers.Loan) {
        RoomSubmitOutbox(dao = get(), serializer = Loan.serializer())
    }
    single<SubmitOutbox<BillReminder>>(qualifier = OutboxQualifiers.BillReminder) {
        RoomSubmitOutbox(dao = get(), serializer = BillReminder.serializer())
    }
    single<SubmitOutbox<LoanCalcScenario>>(qualifier = OutboxQualifiers.LoanCalcScenario) {
        RoomSubmitOutbox(dao = get(), serializer = LoanCalcScenario.serializer())
    }

    // OfflineSubmitSyncer eagerly retries pending drafts when connectivity
    // returns. For purely-local features (no real network), the submitBlock
    // commits to the repository directly — `networkStatusFlow` is still
    // required by the syncer contract.
    //
    // We register each syncer behind a unique marker singleton so Koin's
    // type resolution doesn't collide with other `OfflineSubmitSyncer<*, *>`
    // bindings (PriceAlert below uses the same pattern by virtue of being
    // declared as the bare `OfflineSubmitSyncer` type — see note there).
    single<LoanSubmitSyncer>(createdAtStart = true) {
        LoanSubmitSyncer(
            syncer = OfflineSubmitSyncer<Loan, Loan>(
                scope = get(),
                outbox = get(qualifier = OutboxQualifiers.Loan),
                networkStatusFlow = get<NetworkMonitor>().networkStatus,
                submitBlock = { payload ->
                    get<LoanRepository>().upsert(payload)
                    payload
                },
            ).also { it.start() },
        )
    }
    single<BillReminderSubmitSyncer>(createdAtStart = true) {
        BillReminderSubmitSyncer(
            syncer = OfflineSubmitSyncer<BillReminder, BillReminder>(
                scope = get(),
                outbox = get(qualifier = OutboxQualifiers.BillReminder),
                networkStatusFlow = get<NetworkMonitor>().networkStatus,
                submitBlock = { payload ->
                    get<BillReminderRepository>().upsert(payload)
                    payload
                },
            ).also { it.start() },
        )
    }

    // Fintech Repositories
    single<CurrencyRepository> {
        CurrencyRepositoryImpl(
            exchangeRatesStore = get(AppStoreRegistry.ExchangeRates),
            rateHistoryStore = get(AppStoreRegistry.RateHistory),
            spotRateStore = get(AppStoreRegistry.SpotRate),
            networkMonitor = get(),
            fetchedAtRepository = get(),
        )
    }
    single<CryptoRepository> {
        CryptoRepositoryImpl(
            coinMarketsStore = get(AppStoreRegistry.CoinMarkets),
            coinDetailStore = get(AppStoreRegistry.CoinDetail),
            networkMonitor = get(),
            fetchedAtRepository = get(),
        )
    }

    // cloud-todo — MUTABLE (offline-write) archetype. RoomBookkeeper (owned by core/data) records
    // failed writes for retry-on-reconnect; it's injected into the core/store MutableStore via Koin.
    single<Bookkeeper<CloudTodoKey>> {
        RoomBookkeeper(dao = get(), keySerializer = { "cloudTodo:${it.id}" })
    }
    single<CloudTodoRepository> {
        CloudTodoRepositoryImpl(
            readStore = get(AppStoreRegistry.CloudTodo),
            writeStore = get(AppStoreRegistry.CloudTodoMutable),
            networkMonitor = get(),
            fetchedAtRepository = get(),
        )
    }

    // Economic Repositories (Banking Utility Toolkit — FRED + World Bank)
    single<EconomicRatesRepository> {
        EconomicRatesRepositoryImpl(
            interestRateSeriesStore = get(AppStoreRegistry.InterestRateSeries),
            networkMonitor = get(),
            fetchedAtRepository = get(),
        )
    }
    single<MacroIndicatorsRepository> {
        MacroIndicatorsRepositoryImpl(
            macroIndicatorStore = get(AppStoreRegistry.MacroIndicator),
            networkMonitor = get(),
            fetchedAtRepository = get(),
        )
    }

    // Price alerts — Store-backed (OFFLINE_LOCAL_ONLY archetype).
    // AlertsStore is the source of truth; AlertDao is the write target.
    single { get<AppDatabase>().alertDao }
    single<AlertsRepository> {
        AlertsRepositoryImpl(
            alertsStore = get(AppStoreRegistry.Alerts),
            alertDao = get(),
            networkMonitor = get(),
            fetchedAtRepository = get(),
        )
    }

    // Outbox for PriceAlert payloads — RoomSubmitOutbox writes to framework_submit_drafts.
    single<SubmitOutbox<PriceAlert>>(qualifier = OutboxQualifiers.PriceAlert) {
        RoomSubmitOutbox(dao = get(), serializer = PriceAlert.serializer())
    }

    // Eager singleton — starts watching network online events at Koin start; retries
    // any pending alerts when connectivity returns. For the local-only alerts feature,
    // the submit block commits directly to the repository (simulating offline resilience).
    single(createdAtStart = true) {
        val syncer = OfflineSubmitSyncer<PriceAlert, PriceAlert>(
            scope = get(),
            outbox = get(qualifier = OutboxQualifiers.PriceAlert),
            networkStatusFlow = get<NetworkMonitor>().networkStatus,
            submitBlock = { payload -> get<AlertsRepository>().submitAlert(payload) },
        )
        syncer.start()
        syncer
    }
}

/**
 * Marker singleton wrapping the Loan offline submit syncer.
 *
 * Exists so Koin can resolve the binding by a unique type — bare
 * `OfflineSubmitSyncer<*, *>` would erase to the same runtime [kotlin.reflect.KClass]
 * across every payload type and collide with the PriceAlert syncer.
 */
internal class LoanSubmitSyncer internal constructor(
    @Suppress("unused") val syncer: OfflineSubmitSyncer<Loan, Loan>,
)

/** Marker singleton wrapping the BillReminder offline submit syncer. */
internal class BillReminderSubmitSyncer internal constructor(
    @Suppress("unused") val syncer: OfflineSubmitSyncer<BillReminder, BillReminder>,
)
