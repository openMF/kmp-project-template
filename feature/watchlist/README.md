# :feature:watchlist

Personal crypto watchlist — the canonical `read_local_list` demo, and the read-side companion
to `feature/add-to-watchlist`'s write-side star toggle.

- **Screen:** `WatchlistScreen`.
- **ViewModel:** `WatchlistViewModel(repository: WatchlistRepository)` — a pure passthrough
  exposing `watchlistStream` (`ScreenDataStream<List<WatchlistItem>>`) directly; the stream
  owns Loading/Empty/Content, an empty list yields Empty. Removal (`onRemove(coinId)`) is a
  one-shot local write; the read side re-emits as soon as Room propagates.
- **Route:** `WatchlistRoute` — a single composable destination (no graph nesting). Entry
  point: `NavController.navigateToWatchlist()`.
- **DI:** `WatchlistModule`.

See `FEATURE_AUTHORING.md` for the OFFLINE_LOCAL_ONLY read-side chain.
