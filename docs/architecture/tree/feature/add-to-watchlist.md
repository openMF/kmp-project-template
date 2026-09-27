# `feature/add-to-watchlist/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 27 tracked files — 20× `.xml`, 5× `.kt`, 1× `.md`, 1× `.kts`

**5 Kotlin files** — 0 screen(s), 1 ViewModel(s).

ViewModels: `AddToWatchlistViewModel`

**Consumes:** `core/store/watchlist`

**Store5 archetype(s) exercised:** `CACHE_ONLY` — from `core/store/STORE_ARCHETYPES.yaml`, which product-health fails the build against when an archetype loses its last showcase.

### Docs in the tree

| Path | |
|---|---|
| `feature/add-to-watchlist/README.md` | :feature:add-to-watchlist |

<!-- tree-scaffold:end -->
## Significance

The add-to-watchlist sheet, split out as its own module so the watchlist and the market list can both
present it without depending on each other. Small on purpose: a shared UI surface that lives in one
feature is a dependency the other feature cannot remove.
