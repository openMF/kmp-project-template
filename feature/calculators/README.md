# :feature:calculators

Four loan-planning calculators bundled into one module — mostly pure-compute (no Store),
one wizard opts into an offline draft write.

- **Affordability (B5):** `AffordabilityCalculatorScreen` / `AffordabilityCalculatorViewModel`
  — "no Store, no network" archetype: input state folds to a derived `AffordabilityResult`.
- **Amortization (B3):** `AmortizationScreen` / `AmortizationViewModel` (package
  `amortizationcalc`) — scratch mode or loan-backed (pre-fills from a saved loan by `loanId`).
- **Loan Comparison (B6):** `LoanComparisonScreen` / `LoanComparisonViewModel` — exactly 3
  scenarios compared side-by-side, cheapest-by-total-payable highlighted.
- **Loan Calc Wizard:** `LoanCalcWizardScreen` / `LoanCalcWizardViewModel` — multi-step wizard
  state machine, a `BaseMutationViewModel` (`MutationMode.Draft`) writer via
  `SubmitOutbox<LoanCalcScenario>` (`OutboxQualifiers.LoanCalcScenario`).
- **Routes:** `CalculatorsGraphRoute` (start = `AffordabilityCalculatorRoute`) →
  `AffordabilityCalculatorRoute`, `AmortizationRoute(loanId?)`, `LoanComparisonRoute`,
  `LoanCalcWizardRoute(scenarioId?)`. Entry point: `NavController.navigateToCalculators()`.
- **DI:** `CalculatorsModule`.

See `FEATURE_AUTHORING.md` for the pure-compute chain and the MUTABLE wizard write path.
