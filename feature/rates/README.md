# :feature:rates

B7 Interest Rate Tracker — FRED-backed federal funds / mortgage / treasury series. The
canonical `NETWORK_WITH_CACHE` + DataFreshness showcase.

- **Screens:** `InterestRatesScreen`, `InterestRateDetailScreen`.
- **ViewModels:** `InterestRatesViewModel` — composes four independent FRED-backed reactive
  streams into one dashboard; `InterestRateDetailViewModel` — scoped to a single `seriesId`
  (e.g. `"DFF"`).
- **Routes:** `RatesGraphRoute` → `RatesListRoute` (start), `RateDetailRoute(seriesId)`.
  Entry points: `NavController.navigateToRates()`, `navigateToRateDetail(seriesId)`.
- Add a new FRED series by extending `RateSeriesCatalog` — no client changes needed;
  `RateStreamFactory` builds the per-series stream.
- **DI:** `RatesModule`.

See `FEATURE_AUTHORING.md` for the NETWORK_WITH_CACHE archetype chain and freshness signal.
