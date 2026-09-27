# `feature/bills/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 42 tracked files — 20× `.xml`, 20× `.kt`, 1× `.md`, 1× `.kts`

**20 Kotlin files** — 2 screen(s), 2 ViewModel(s).

Screens: `AddOrEditBillReminderScreen`, `BillRemindersListScreen`

ViewModels: `BillRemindersListViewModel`, `EditBillReminderViewModel`

**Consumes:** `core/store/banking`, `core/store/config`

**Store5 archetype(s) exercised:** `CACHE_ONLY`, `LOAD_ONCE`, `OFFLINE_LOCAL_ONLY` — from `core/store/STORE_ARCHETYPES.yaml`, which product-health fails the build against when an archetype loses its last showcase.

### Docs in the tree

| Path | |
|---|---|
| `feature/bills/README.md` | :feature:bills |

<!-- tree-scaffold:end -->
## Significance

Recurring bill reminders, and the showcase for **offline-resilient form submission**. The form is
`DraftSubmitHandler`-backed and the reminder itself is scheduled through the platform notification
seam, so the feature spans `core/data` and `core/platform` rather than staying in one layer.

`dueDay` is a day-of-month clamped to the month's length at read time, so 31 is safe in February — the
kind of detail a screen must not re-derive per render.
