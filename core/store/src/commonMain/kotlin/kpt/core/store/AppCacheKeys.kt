/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store

/**
 * Single source of truth for every `asScreenStream` / `asPagingScreenStream` **cacheKey** — the
 * string that keys per-stream fetched-at (freshness) tracking. Owned here in `core/store` next to
 * [AppStoreRegistry], so the key strings live in ONE place and cannot drift or silently collide.
 *
 * `core/data` repositories reference these constants + typed builders instead of inlining string
 * literals at the call site — a repo does `cacheKey = AppCacheKeys.LOANS` or
 * `cacheKey = AppCacheKeys.loan(id)`, never `cacheKey = "loan:$id"`. The format of a key lives here;
 * a call site only supplies the values. A fork adds one line per new stream, next to its store
 * qualifier in [AppStoreRegistry].
 */
object AppCacheKeys {
    // demo:begin — whole-list / single-instance streams (static keys).
    const val ALERTS = "alerts"
    const val WATCHLIST = "watchlist"
    const val LOANS = "loans"
    const val BILL_REMINDERS = "billReminders"
    const val COIN_MARKETS = "crypto:coinMarkets"

    // Per-key streams — typed builders own the format string; the call site passes only the values.
    fun loan(id: String): String = "loan:$id"

    fun coinDetail(coinId: String): String = "crypto:coinDetail:$coinId"

    fun cloudTodo(id: Int): String = "cloudTodo:$id"

    fun exchangeRates(baseCurrency: String): String = "currency:exchangeRates:$baseCurrency"

    fun spotRate(baseCurrency: String): String = "currency:spotRate:$baseCurrency"

    fun rateHistory(from: String, to: String, days: Int): String = "currency:rateHistory:$from-$to-${days}d"

    fun interestRateSeries(seriesId: String, days: Int): String = "economic:rates:$seriesId:${days}d"

    fun macroIndicator(countryCode: String, indicator: String, years: Int): String =
        "economic:macro:$countryCode:$indicator:${years}y"
    // demo:end
}
