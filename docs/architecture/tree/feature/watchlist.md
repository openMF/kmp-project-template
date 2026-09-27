# `feature/watchlist/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 31 tracked files — 20× `.xml`, 9× `.kt`, 1× `.md`, 1× `.kts`

**9 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `WatchlistScreen`

ViewModels: `WatchlistViewModel`

**Consumes:** `core/store/watchlist`

**Store5 archetype(s) exercised:** `CACHE_ONLY` — from `core/store/STORE_ARCHETYPES.yaml`, which product-health fails the build against when an archetype loses its last showcase.

### Docs in the tree

| Path | |
|---|---|
| `feature/watchlist/README.md` | :feature:watchlist |

<!-- tree-scaffold:end -->
## Significance

A user-curated coin list — **`CACHE_ONLY`**, entirely local, no network of its own. It reads the same
`core/store/crypto` prices the market list does, so a watched coin never shows a different number than
the list it was added from.
