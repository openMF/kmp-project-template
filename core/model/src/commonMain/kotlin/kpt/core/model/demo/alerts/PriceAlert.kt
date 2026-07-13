/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.demo.alerts

import kotlinx.serialization.Serializable

/**
 * A price alert configured by the user for a specific coin.
 *
 * Serializable because [kpt.core.data.demo.alerts.AlertsRepository] persists
 * unsent alerts to the `framework_submit_drafts` outbox during offline
 * submission — RoomSubmitOutbox writes the payload as JSON.
 *
 * @property id Stable identifier (UUID or other client-generated id). Used as the
 *   primary key both client-side and on the fake server.
 * @property coinId CoinGecko identifier (e.g., "bitcoin", "ethereum").
 * @property direction Above / Below / PCT_CHANGE comparison applied to [targetValue].
 * @property targetValue Threshold value — interpreted in USD for ABOVE/BELOW,
 *   in percentage for PCT_CHANGE.
 * @property enabled Whether the alert is currently active. Disabled alerts persist
 *   but don't trigger notifications.
 * @property createdAtMs Epoch millis when the alert was first saved (client time).
 */
@Serializable
data class PriceAlert(
    val id: String,
    val coinId: String,
    val direction: AlertDirection,
    val targetValue: Double,
    val enabled: Boolean = true,
    val createdAtMs: Long,
)

@Serializable
enum class AlertDirection {
    /** Trigger when price rises above [PriceAlert.targetValue]. */
    ABOVE,

    /** Trigger when price falls below [PriceAlert.targetValue]. */
    BELOW,

    /** Trigger on a percent-change of at least [PriceAlert.targetValue]% over 24h. */
    PCT_CHANGE,
}
