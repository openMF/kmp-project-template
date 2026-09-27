# `feature/alerts/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 35 tracked files — 20× `.xml`, 13× `.kt`, 1× `.md`, 1× `.kts`

**13 Kotlin files** — 2 screen(s), 2 ViewModel(s).

Screens: `AlertCreateScreen`, `AlertsListScreen`

ViewModels: `AlertCreateViewModel`, `AlertsListViewModel`

**Consumes:** `core/store/alerts`, `core/store/config`

**Store5 archetype(s) exercised:** `CACHE_ONLY`, `OFFLINE_LOCAL_ONLY` — from `core/store/STORE_ARCHETYPES.yaml`, which product-health fails the build against when an archetype loses its last showcase.

### Docs in the tree

| Path | |
|---|---|
| `feature/alerts/README.md` | :feature:alerts |

<!-- tree-scaffold:end -->
## Significance

Price alerts — a **draft-backed create form** plus a local alert store. `targetValueText` is held as raw
text rather than a parsed number so a partially typed value survives recomposition; parsing happens
once, on submit.
