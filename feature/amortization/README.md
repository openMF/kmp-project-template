# :feature:amortization

Full month-by-month payment schedule for an existing loan — a read-side, OFFLINE_LOCAL_ONLY
projection over `LoanRepository`, no network call and no Room write.

- **Screen:** `AmortizationScheduleScreen`.
- **ViewModel:** `AmortizationScheduleViewModel(repository: LoanRepository, loanId: String)` —
  derives the schedule from the current loan snapshot; plain `LazyColumn` handles the finite
  (≤ 360-row) list, so `PagingScreenStream` is deliberately not used here.
- **Route:** `AmortizationScheduleRoute(loanId)` — a single destination (no graph wrapper),
  wired into a host graph via `amortizationScheduleDestination(navController)`; call
  `NavController.navigateToAmortizationSchedule(loanId)` to reach it.
- **DI:** `AmortizationModule`.

Consumed by both `feature/loans` (`LoanDetailScreen`'s "view amortization" action) and
`feature/calculators` (its own `AmortizationScreen`, in loan-backed mode). See
`FEATURE_AUTHORING.md` for the OFFLINE_LOCAL_ONLY archetype chain.
