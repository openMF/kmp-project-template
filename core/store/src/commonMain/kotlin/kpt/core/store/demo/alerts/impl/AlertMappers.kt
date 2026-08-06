/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.demo.alerts.impl

import kpt.core.database.demo.alerts.AlertEntity
import kpt.core.model.demo.alerts.AlertDirection
import kpt.core.model.demo.alerts.PriceAlert

/**
 * Entity ⇄ domain mappers for the price-alerts feature. They live in `core/store` (the lowest
 * layer that sees BOTH the Room [AlertEntity] and the domain [PriceAlert]) so the [provideAlertsStore]
 * `SourceOfTruth` can emit the DOMAIN model (never an `*Entity`) per the read-path contract —
 * FEATURE_LAYER.md "Read-path contract (ALL read shapes — the one way)". `AlertsRepositoryImpl`
 * consumes [toAlertEntity] for its write path.
 *
 * Field-level mapping:
 * - [AlertEntity.symbol] ⇄ [PriceAlert.coinId]
 * - [AlertEntity.conditionAbove] ⇄ [PriceAlert.direction] (true → ABOVE, false → BELOW; PCT_CHANGE → false)
 * - [AlertEntity.targetPrice] ⇄ [PriceAlert.targetValue]
 * - [AlertEntity.createdAt] ⇄ [PriceAlert.createdAtMs]
 * - [PriceAlert.enabled] defaults to `true` (AlertEntity has no enabled field)
 */
fun AlertEntity.toPriceAlert(): PriceAlert = PriceAlert(
    id = id,
    coinId = symbol,
    direction = if (conditionAbove) AlertDirection.ABOVE else AlertDirection.BELOW,
    targetValue = targetPrice,
    enabled = true,
    createdAtMs = createdAt,
)

/** @see toPriceAlert — the persistable inverse used by the repository's write path. */
fun PriceAlert.toAlertEntity(): AlertEntity = AlertEntity(
    id = id,
    symbol = coinId,
    conditionAbove = direction == AlertDirection.ABOVE,
    targetPrice = targetValue,
    createdAt = createdAtMs,
)
