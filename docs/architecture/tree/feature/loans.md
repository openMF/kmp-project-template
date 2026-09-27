# `feature/loans/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 48 tracked files — 26× `.kt`, 20× `.xml`, 1× `.md`, 1× `.kts`

**26 Kotlin files** — 3 screen(s), 3 ViewModel(s).

Screens: `AddOrEditLoanScreen`, `LoanDetailScreen`, `PersonalLoansListScreen`

ViewModels: `EditLoanViewModel`, `LoanDetailViewModel`, `PersonalLoansListViewModel`

**Consumes:** `core/store/banking`, `core/store/config`

**Store5 archetype(s) exercised:** `CACHE_ONLY`, `LOAD_ONCE`, `OFFLINE_LOCAL_ONLY` — from `core/store/STORE_ARCHETYPES.yaml`, which product-health fails the build against when an archetype loses its last showcase.

### Docs in the tree

| Path | |
|---|---|
| `feature/loans/README.md` | :feature:loans |

<!-- tree-scaffold:end -->
## Significance

Personal loan tracking — and the template's reference for a **paging list plus a draft-backed edit
form**. `PersonalLoansListScreen` is `PagingScreenContent`; `AddOrEditLoanScreen` is
`DraftSubmitHandler`, so a part-filled loan survives process death.

It is a *tracker*, not a ledger: `principalRemaining`, `monthsRemaining` and `totalPaid` are
user-maintained, not derived from a payment history. That is a deliberate scope line — a "log a payment"
feature would recompute them, and until it exists the fields must not pretend to be computed.
