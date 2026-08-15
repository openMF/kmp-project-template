# :feature:crypto

Paginated crypto coin-markets list — the `PagingScreenStream` showcase.

- **Screen:** `CoinMarketsScreen`, wrapped by the nav-facing `CoinMarketsRoute` composable
  (split out so the graph has a stable no-arg destination target).
- **ViewModel:** `CoinMarketsViewModel(repository: CryptoRepository)` — exposes a
  `PagingScreenStream<CoinMarket>` (page size 20); `retry()` resets the paging cursor.
  Scroll position is a pure Compose concern (`rememberLazyListState`), not VM state.
- **Route:** `CryptoGraphRoute` (start = `CoinMarketsListRoute`). Entry point:
  `NavController.navigateToCrypto()`.
- Each row hosts `feature/add-to-watchlist`'s embedded `AddToWatchlistStar` toggle
  (`implementation(projects.feature.addToWatchlist)`); coin-tap currently no-ops pending a
  future coin-detail destination.
- **DI:** `CryptoFeatureModule`.

See `FEATURE_AUTHORING.md` for the paginated-list archetype chain.
