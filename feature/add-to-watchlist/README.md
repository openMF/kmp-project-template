# :feature:add-to-watchlist

Embedded write-side widget — a single star icon-button (`AddToWatchlistStar`) that toggles
a coin's membership in the personal crypto watchlist. Not a navigable screen: it is hosted
inline on `feature/crypto`'s CoinMarkets rows via `implementation(projects.feature.addToWatchlist)`.

- **Composable:** `AddToWatchlistStar(coinId)` — filled `Star` when tracked, outline
  `StarBorder` otherwise.
- **ViewModel:** `AddToWatchlistViewModel(repository, coinId)` — keyed per `coinId` via
  Koin `koinViewModel(key = coinId) { parametersOf(coinId) }`, one instance per hosting row.
- Read side: `WatchlistRepository.contains(coinId)` drives `isTracked`. Write side: `onToggle()`
  routes through a `submitHandler<Unit>()` — the canonical `submit_offline_write` (SubmitHandler)
  reference — calling `repository.add`/`repository.remove`.

No nav routes — there is no `*Navigation.kt` in this module. DI: `AddToWatchlistModule`
(parametrised `viewModel { params -> ... }`).

See `FEATURE_AUTHORING.md` for the full archetype → module chain, and `feature/watchlist` for
the companion read-side list screen.
