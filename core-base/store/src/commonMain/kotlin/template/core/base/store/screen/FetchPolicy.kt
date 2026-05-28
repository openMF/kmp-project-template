/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.store.screen

/**
 * Controls whether a screen stream reads from cache, hits the network, or both.
 *
 * Pass to [ScreenDataStream.asScreenStream], [LoadOnceStream.asLoadOnceStream], or
 * [PagingScreenStream] to override the default cache-then-network behaviour.
 *
 * **Choosing a policy:**
 * | Scenario                                                                    | Policy                          |
 * |-----------------------------------------------------------------------------|---------------------------------|
 * | Normal screen — show cached data immediately, refresh in background         | [CACHE_THEN_NETWORK] (default)  |
 * | Online-first — try network, gracefully fall back to cache when network fails | [NETWORK_THEN_CACHE_FALLBACK]   |
 * | Always-fresh data required (e.g. payment confirmation)                      | [NETWORK_ONLY]                  |
 * | Offline-only view or explicit "load from cache"                             | [CACHE_ONLY]                    |
 *
 * Modelled as a sealed interface so the vocabulary is open to additional variants
 * without breaking the source-compatible `FetchPolicy.CACHE_THEN_NETWORK`-style
 * access pattern.
 */
sealed interface FetchPolicy {

    /**
     * Emit cached data immediately (if present), then trigger a background network fetch
     * and emit refreshed data when it arrives.
     *
     * This is the default and works well for most screens. The user sees content quickly
     * while the data is silently refreshed in the background. The staleness banner from
     * [DataFreshnessIndicator] is shown when the data is older than the configured TTL.
     */
    data object CACHE_THEN_NETWORK : FetchPolicy

    /**
     * Try the network first; if it fails, fall back to the local source of truth.
     *
     * Use for screens that prefer fresh data but should remain functional offline —
     * e.g. an account summary that wants the latest balance but is fine with the
     * last-cached value during a network outage. Unlike [NETWORK_ONLY], failure
     * does NOT emit [ScreenState.NoNetwork]; instead, Store5 serves the
     * SourceOfTruth value and downstream [DecisionEngine] decides freshness
     * (typically `STALE` while offline).
     *
     * Internally maps to `StoreReadRequest.fresh(key, fallBackToSourceOfTruth = true)`
     * — the same request shape as [NETWORK_ONLY]. The semantic difference is one of
     * intent: [NETWORK_THEN_CACHE_FALLBACK] documents that cache-fallback is the
     * expected happy path during transient network failure, whereas [NETWORK_ONLY]
     * is reserved for screens where stale data would be actively misleading.
     */
    data object NETWORK_THEN_CACHE_FALLBACK : FetchPolicy

    /**
     * Skip the local cache entirely and always fetch from the network.
     *
     * Use for screens where stale data would be misleading or harmful (e.g. payment status,
     * balance after a transaction). If the network fails the stream emits
     * [ScreenState.NoNetwork] or [ScreenState.Error] instead of showing stale data.
     */
    data object NETWORK_ONLY : FetchPolicy

    /**
     * Read only from the local cache; never perform a network request.
     *
     * Use for explicitly offline screens or when the calling code knows connectivity is
     * unavailable and wants to avoid error flicker. If the cache is empty the stream emits
     * [ScreenState.Empty].
     *
     * **DataFreshnessIndicator behaviour:** the staleness banner will still show the
     * `lastFetchedAt` timestamp from cache — even if it is very old — because no background
     * refresh is triggered to update it. The staleness threshold is applied as normal; if the
     * cached data is older than the TTL, the stale banner renders. To suppress the banner on
     * explicitly offline screens, pass `showFreshnessIndicator = false` to [ScreenContent].
     */
    data object CACHE_ONLY : FetchPolicy

    /**
     * Cache-then-network on the read side, plus a periodic background refresh.
     *
     * Use for ambient surfaces that should silently re-fetch on a cadence — live FX
     * rates, market tickers, dashboard tiles. The read semantic is identical to
     * [CACHE_THEN_NETWORK] (cached value emitted immediately, network refresh
     * layered in); the periodic refresh is fired by [ScreenDataStream] internally
     * via the same refresh-trigger pipeline as the reconnect refresh and the
     * user-tap refresh — so it goes through the user-tap debounce window, the
     * staleness banner, and the lastContent preservation logic for free.
     *
     * **Tuning notes:**
     * - [intervalMillis] must be positive. Values shorter than ~5s on mobile
     *   risk battery drain; values shorter than the
     *   [DEFAULT_USER_REFRESH_DEBOUNCE_MS] (1s) will be coalesced by the
     *   user-tap debounce anyway.
     * - [foregroundOnly] is wired as a flag today but the foreground-aware
     *   gating is deferred to a follow-up phase (needs `LocalAppForegroundFlow`
     *   threading through core-base/ui). The ticker fires unconditionally for
     *   now; the flag is parsed and held so callers can declare intent without
     *   waiting for the runtime gate.
     *
     * @property intervalMillis Refresh cadence in milliseconds. Must be `> 0`.
     * @property foregroundOnly If true (default), refresh only when the app is
     *   in the foreground. Currently informational — see "Tuning notes".
     */
    data class PERIODIC(
        val intervalMillis: Long,
        val foregroundOnly: Boolean = true,
    ) : FetchPolicy {
        init {
            require(intervalMillis > 0L) { "intervalMillis must be positive (got $intervalMillis)" }
        }
    }
}
