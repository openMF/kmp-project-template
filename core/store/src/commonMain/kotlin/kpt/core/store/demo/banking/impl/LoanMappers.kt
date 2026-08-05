/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.demo.banking.impl

import kpt.core.database.demo.banking.entity.LoanEntity
import kpt.core.model.demo.banking.Loan

/**
 * Entity ⇄ domain mappers for the loans feature. They live in `core/store` (the lowest layer that
 * sees BOTH the Room [LoanEntity] and the domain [Loan]) so [provideLoansStore]'s `SourceOfTruth`
 * can emit the DOMAIN model per the read-path contract; `LoanRepositoryImpl` consumes them for its
 * DAO-direct reads (`observeById`/`getById`) and its write path ([toEntity]).
 */
fun LoanEntity.toDomain(): Loan = Loan(
    id = id,
    name = name,
    kind = kind,
    principal = principal,
    principalRemaining = principalRemaining,
    annualRatePercent = annualRatePercent,
    tenureMonths = tenureMonths,
    monthsRemaining = monthsRemaining,
    monthlyPayment = monthlyPayment,
    nextDueDate = nextDueDate,
    totalPaid = totalPaid,
    createdAtMs = createdAtMs,
    updatedAtMs = updatedAtMs,
)

/** @see toDomain — the persistable inverse used by the repository's write path. */
fun Loan.toEntity(): LoanEntity = LoanEntity(
    id = id,
    name = name,
    kind = kind,
    principal = principal,
    principalRemaining = principalRemaining,
    annualRatePercent = annualRatePercent,
    tenureMonths = tenureMonths,
    monthsRemaining = monthsRemaining,
    monthlyPayment = monthlyPayment,
    nextDueDate = nextDueDate,
    totalPaid = totalPaid,
    createdAtMs = createdAtMs,
    updatedAtMs = updatedAtMs,
)
