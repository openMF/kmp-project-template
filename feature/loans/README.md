# :feature:loans

Personal loan tracker — list, detail, and add/edit. The canonical
`PagingScreenStream`-list-plus-`SubmitHandler`-edit-form reference, and the primary MUTABLE
archetype showcase.

- **Screens:** `PersonalLoansListScreen`, `LoanDetailScreen`, `AddOrEditLoanScreen`.
- **ViewModels:** `PersonalLoansListViewModel` — combines `loansStream` with two dashboard
  totals (`observeTotalMonthlyEmi`, `observeTotalPrincipalRemaining`); `LoanDetailViewModel`
  — single-loan observe, routes to edit/amortization; `EditLoanViewModel` — **the canonical
  multi-formKey `BaseMutationViewModel` (`MutationMode.Draft`) showcase**, draft-persisted via
  `SubmitOutbox<Loan>` (`DemoOutboxQualifiers.Loan`).
- **Routes:** `LoansGraphRoute` → `PersonalLoansListRoute` (start), `LoanDetailRoute(loanId)`,
  `AddOrEditLoanRoute(loanId?)`. Entry point: `NavController.navigateToLoans()`. The detail's
  "view amortization" action is the `onAmortizationClick` callback of `loansGraph`, which the app
  shell wires to `feature/amortization` in `ForkFeatureRegistry` — features never depend on each other.
- `LoanReminderUseCase` is the cross-module proof-of-concept consuming `sync`'s
  `WorkScheduler` to schedule due-date notifications.
- **DI:** `LoansModule` — VMs stay factory-bound (`viewModel {}`, never `single`) per the
  D14 back-stack-scoping fence.

See `FEATURE_AUTHORING.md` for the MUTABLE archetype module chain.
