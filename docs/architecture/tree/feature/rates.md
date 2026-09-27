# `feature/rates/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 38 tracked files — 20× `.xml`, 16× `.kt`, 1× `.md`, 1× `.kts`

**16 Kotlin files** — 2 screen(s), 2 ViewModel(s).

Screens: `InterestRateDetailScreen`, `InterestRatesScreen`

ViewModels: `InterestRateDetailViewModel`, `InterestRatesViewModel`

**Consumes:** `core/store/economic`

**Store5 archetype(s) exercised:** `MEMORY_ONLY`, `NETWORK_WITH_CACHE` — from `core/store/STORE_ARCHETYPES.yaml`, which product-health fails the build against when an archetype loses its last showcase.

### Docs in the tree

| Path | |
|---|---|
| `feature/rates/README.md` | :feature:rates |

<!-- tree-scaffold:end -->
## Significance

FRED-backed interest rates — federal funds, prime, 30-year mortgage, 10-year treasury — each its own
independent `ScreenState` combined into one dashboard.

The point is the **4-way fan-in**: one slow or failing series must not blank the other three. Adding a
series means extending `RateSeriesCatalog.kt`; no client change is needed, which is the seam a fork
exercises when it points the feature at its own data.
