# `feature/macro/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 44 tracked files — 22× `.kt`, 20× `.xml`, 1× `.md`, 1× `.kts`

**22 Kotlin files** — 3 screen(s), 3 ViewModel(s).

Screens: `CountryMacroScreen`, `CountryPickerScreen`, `MacroIndicatorDetailScreen`

ViewModels: `CountryMacroViewModel`, `CountryPickerViewModel`, `MacroIndicatorDetailViewModel`

**Consumes:** `core/store/economic`

**Store5 archetype(s) exercised:** `MEMORY_ONLY`, `NETWORK_WITH_CACHE` — from `core/store/STORE_ARCHETYPES.yaml`, which product-health fails the build against when an archetype loses its last showcase.

### Docs in the tree

| Path | |
|---|---|
| `feature/macro/README.md` | :feature:macro |

<!-- tree-scaffold:end -->
## Significance

World Bank country indicators — GDP, CPI, unemployment — and the **multi-source combine with a country
picker**.

Three cards, three independent states, and an aggregate freshness band so the TopAppBar can show one
STALE badge without a caller folding the three by hand. The picker round-trips a country code through
navigation rather than holding it in a shared singleton, which is what keeps the screen restorable.
