# :feature:loans

Personal loan tracker — list, detail, and add/edit. The canonical
`PagingScreenStream`-list-plus-`SubmitHandler`-edit-form reference, and the primary MUTABLE
archetype showcase.

- **Screens:** `PersonalLoansListScreen`, `LoanDetailScreen`, `AddOrEditLoanScreen`.
- **ViewModels:** `PersonalLoansListViewModel` — combines `loansStream` with two dashboard
  totals (`observeTotalMonthlyEmi`, `observeTotalPrincipalRemaining`); `LoanDetailViewModel`
  — single-loan observe, routes to edit/amortization; `EditLoanViewModel` — **the canonical
  multi-formKey `BaseMutationViewModel` (`MutationMode.Draft`) showcase**, draft-persisted via
  `SubmitOutbox<Loan>` (`OutboxQualifiers.Loan`).
- **Routes:** `LoansGraphRoute` → `PersonalLoansListRoute` (start), `LoanDetailRoute(loanId)`,
  `AddOrEditLoanRoute(loanId?)`; the graph also mounts `feature/amortization`'s
  `amortizationScheduleDestination`. Entry point: `NavController.navigateToLoans()`.
- `LoanReminderUseCase` is the cross-module proof-of-concept consuming `sync`'s
  `WorkScheduler` to schedule due-date notifications.
- **DI:** `LoansModule` — VMs stay factory-bound (`viewModel {}`, never `single`) per the
  D14 back-stack-scoping fence.

See `FEATURE_AUTHORING.md` for the MUTABLE archetype module chain.
