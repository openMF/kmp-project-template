# `feature/amortization/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 31 tracked files — 20× `.xml`, 9× `.kt`, 1× `.md`, 1× `.kts`

**9 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `AmortizationScheduleScreen`

ViewModels: `AmortizationScheduleViewModel`

**Consumes:** `core/store/banking`

**Store5 archetype(s) exercised:** `CACHE_ONLY`, `LOAD_ONCE`, `OFFLINE_LOCAL_ONLY` — from `core/store/STORE_ARCHETYPES.yaml`, which product-health fails the build against when an archetype loses its last showcase.

### Docs in the tree

| Path | |
|---|---|
| `feature/amortization/README.md` | :feature:amortization |

<!-- tree-scaffold:end -->
## Significance

The month-by-month payment schedule for a tracked loan — **`OFFLINE_LOCAL_ONLY`**, a projection of the
loan repository with no network involved.

Its cost scales with tenure: a 30-year loan is 360 rows. That is why the schedule is Store-backed
despite being computed — a revisit is free, and `SCHEDULE_ROWS` is a crash key precisely because an
out-of-memory failure here scales with that number.
