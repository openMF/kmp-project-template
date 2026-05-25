/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.data.di

import org.koin.core.qualifier.named

/**
 * Named qualifiers for the four `SubmitOutbox<*>` bindings.
 *
 * Why qualifiers?
 *
 * Koin indexes `single<T>` definitions by `T::class` (the raw type), not by the full
 * `KType`. So `single<SubmitOutbox<Loan>>`, `single<SubmitOutbox<BillReminder>>`,
 * `single<SubmitOutbox<LoanCalcScenario>>`, and `single<SubmitOutbox<PriceAlert>>` all
 * collide under `SubmitOutbox::class` — the LAST registered wins. Every consumer that
 * calls `get<SubmitOutbox<Foo>>()` actually receives whichever outbox was registered last,
 * regardless of the generic parameter. At first `.saveByUniqueKey(payload)` call, the
 * wrong serializer fires:
 *
 *   `ClassCastException: org.mifos.core.model.banking.LoanCalcScenario
 *    cannot be cast to org.mifos.core.model.alerts.PriceAlert`
 *
 * Fix: every outbox `single<>` registration declares `qualifier = OutboxQualifiers.X`
 * and every consumer uses `get(qualifier = OutboxQualifiers.X)`. Then the lookup keys
 * are unambiguous: `(SubmitOutbox::class, "outbox.loan")` ≠
 * `(SubmitOutbox::class, "outbox.priceAlert")`.
 *
 * Add a new payload type? Add a new constant here, register its `single<>` with the
 * qualifier, and have the consuming feature module fetch with the same qualifier.
 */
object OutboxQualifiers {
    /** `SubmitOutbox<org.mifos.core.model.banking.Loan>`. */
    val Loan = named("outbox.loan")

    /** `SubmitOutbox<org.mifos.core.model.banking.BillReminder>`. */
    val BillReminder = named("outbox.billReminder")

    /** `SubmitOutbox<org.mifos.core.model.banking.LoanCalcScenario>`. */
    val LoanCalcScenario = named("outbox.loanCalcScenario")

    /** `SubmitOutbox<org.mifos.core.model.alerts.PriceAlert>`. */
    val PriceAlert = named("outbox.priceAlert")
}
