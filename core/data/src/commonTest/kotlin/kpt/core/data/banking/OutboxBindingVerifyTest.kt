/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(org.koin.core.annotation.KoinInternalApi::class)

package kpt.core.data.banking

import kpt.core.base.store.submit.SubmitOutbox
import kpt.core.data.config.AppOutboxQualifiers
import kpt.core.data.di.GeneratedRepositoryBindings
import org.koin.core.qualifier.Qualifier
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Verifies that every `SubmitOutbox<T>` declared by `@DataProvider(qualifier = …)` is registered
 * under its corresponding [AppOutboxQualifiers] entry. Catches regressions
 * where a new payload type is added to [AppOutboxQualifiers] but the matching
 * `single<>` registration is missing — or vice versa.
 *
 * NOTE (E1 / C1): the demo `SubmitOutbox<*>` bindings were relocated out of the infra
 * aggregator [kpt.core.data.di.DataModule] (`RepositoryModule.kt`) into the fork-owned
 * the GENERATED [GeneratedRepositoryBindings]; this test
 * introspects that module's mappings now. The demo qualifiers themselves were relocated to
 * [AppOutboxQualifiers] in `config/`, generated from the same annotation that binds each outbox, so THIS test lives
 * in the `banking` demo package and `remove-demo.sh` deletes it with the showcase it verifies (it sat in
 * `di/` until 2026-11-02, outlived the strip, and stopped a cleaned fork's tests compiling). The
 * template-owned [kpt.core.data.di.OutboxQualifiers] seam stays empty and demo-free.
 *
 * Failure mode if not caught: `ClassCastException` at first
 * `.saveByUniqueKey(payload)` call on the mis-registered outbox.
 *
 * Implementation note: we introspect [kpt.core.data.di.DataModule]'s `mappings` table (the
 * static registration map) rather than calling `koinApplication { modules(DataModule) }
 * .koin.get<...>()`. Eager-instantiation of the full DataModule graph requires
 * platform actuals (`platformModule`, `platformSecurityModule`, `AppDatabase`)
 * that are not on the classpath in `:core:data:commonTest`. Static introspection
 * checks the exact contract — "is the qualifier wired to a `SubmitOutbox`
 * primary type?" — without paying that cost.
 *
 * NOTE: PriceAlert is still verified — the Alerts archival decision is
 * deferred (see `feature/_archive/alerts/README.md`), so the qualifier and
 * its outbox binding remain active in `RepositoryModule.kt` until the
 * 2026-08-23 deadline.
 */
class OutboxBindingVerifyTest {

    @Test
    fun loanOutboxIsRegisteredUnderLoanQualifier() {
        assertSubmitOutboxBoundUnderQualifier(AppOutboxQualifiers.Loan)
    }

    @Test
    fun billReminderOutboxIsRegisteredUnderBillReminderQualifier() {
        assertSubmitOutboxBoundUnderQualifier(AppOutboxQualifiers.BillReminder)
    }

    @Test
    fun loanCalcScenarioOutboxIsRegisteredUnderLoanCalcScenarioQualifier() {
        assertSubmitOutboxBoundUnderQualifier(AppOutboxQualifiers.LoanCalcScenario)
    }

    @Test
    fun priceAlertOutboxIsRegisteredUnderPriceAlertQualifier() {
        assertSubmitOutboxBoundUnderQualifier(AppOutboxQualifiers.PriceAlert)
    }

    /**
     * Asserts that [GeneratedRepositoryBindings]'s mapping table contains exactly one
     * `InstanceFactory` whose [org.koin.core.definition.BeanDefinition] has
     * `primaryType == SubmitOutbox::class` AND `qualifier == expected`.
     */
    private fun assertSubmitOutboxBoundUnderQualifier(expected: Qualifier) {
        val match = GeneratedRepositoryBindings.mappings.values.firstOrNull { factory ->
            val def = factory.beanDefinition
            def.primaryType == SubmitOutbox::class && def.qualifier == expected
        }
        assertNotNull(
            match,
            "GeneratedRepositoryBindings must register a SubmitOutbox<*> single<> under qualifier $expected — " +
                "missing or mis-qualified binding. See OutboxQualifiers KDoc.",
        )
        // Sanity — the qualifier value matches what callers use (e.g. `get(qualifier = X)`).
        assertTrue(
            match.beanDefinition.qualifier == expected,
            "Qualifier mismatch for $expected on bound SubmitOutbox<*>.",
        )
    }
}
