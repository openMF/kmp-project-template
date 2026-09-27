# `feature/crypto/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 36 tracked files — 20× `.xml`, 14× `.kt`, 1× `.md`, 1× `.kts`

**14 Kotlin files** — 2 screen(s), 2 ViewModel(s).

Screens: `CoinDetailScreen`, `CoinMarketsScreen`

ViewModels: `CoinDetailViewModel`, `CoinMarketsViewModel`

**Consumes:** `core/store/crypto`

### Docs in the tree

| Path | |
|---|---|
| `feature/crypto/README.md` | :feature:crypto |

<!-- tree-scaffold:end -->
## Significance

CoinGecko market list and coin detail — the **paging** showcase over a real remote API.

`PagingScreenStream` accumulates pages, and the cached rows carry the `page` they arrived on so a
targeted refresh can evict one page rather than the whole list. The detail screen is reachable by deep
link, which is why `CoinDetail` carries the market rank: a detail opened cold must not need the list.
