# :feature:currency-rates

Live FX rates by base currency, plus historical rate charts — the CACHE_ONLY / NETWORK_ONLY
policy-routing showcase.

- **Screens:** `CurrencyRatesScreen`, `RateHistoryScreen`.
- **ViewModels:** `CurrencyRatesViewModel` — routes `spotConversionRate` on `NetworkMonitor`
  status: online → `FetchPolicy.NETWORK_ONLY` (always-fresh, no stale cache), offline →
  `FetchPolicy.CACHE_ONLY` (cached value, no API call); also feeds the home dashboard's
  PERIODIC exchange-rate tile. `RateHistoryViewModel` — a dynamic-key flow
  (`RateHistoryKey(from, to, days)`) that auto-refreshes on currency/period change and carries
  the previous selection forward as a stale fallback on fetch failure.
- **Routes:** `CurrencyRatesGraphRoute` → `CurrencyRatesRoute` (start), `RateHistoryRoute`.
  Entry points: `NavController.navigateToCurrencyRates()`, `navigateToRateHistory()`.
- **DI:** `CurrencyRatesModule`.

See `FEATURE_AUTHORING.md` for the CACHE_ONLY / NETWORK_ONLY archetype rows.
