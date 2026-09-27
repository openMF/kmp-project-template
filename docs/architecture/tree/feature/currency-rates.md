# `feature/currency-rates/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 37 tracked files — 20× `.xml`, 15× `.kt`, 1× `.md`, 1× `.kts`

**15 Kotlin files** — 2 screen(s), 2 ViewModel(s).

Screens: `CurrencyRatesScreen`, `RateHistoryScreen`

ViewModels: `CurrencyRatesViewModel`, `RateHistoryViewModel`

**Consumes:** `core/store/config`, `core/store/currency`

**Store5 archetype(s) exercised:** `NETWORK_WITH_CACHE`, `PERIODIC` — from `core/store/STORE_ARCHETYPES.yaml`, which product-health fails the build against when an archetype loses its last showcase.

### Docs in the tree

| Path | |
|---|---|
| `feature/currency-rates/README.md` | :feature:currency-rates |

<!-- tree-scaffold:end -->
## Significance

Live FX rates plus a historical chart — the **`NETWORK_WITH_CACHE`** showcase, and the one place a
windowed series is keyed correctly.

The chart's window length is *part of the store key*, so widening it is a different key and a full
re-fetch rather than a page append. Getting that wrong produces a chart that silently shows a short
window's data under a long window's axis.
