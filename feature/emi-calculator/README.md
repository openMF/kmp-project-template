# :feature:emi-calculator

Single-screen EMI calculator — the canonical "pure local state, no Store" reference.

- **Screen:** `EmiCalculatorScreen`.
- **ViewModel:** `EmiCalculatorViewModel` (no constructor args) — holds `EmiState`
  (principal / ratePercent / tenureMonths) and derives `emiResult: StateFlow<EmiResult?>` via
  `core/domain`'s `calculateEmi` use case whenever all three inputs are positive.
- **Route:** `EmiCalculatorRoute` — a single composable destination wired via
  `emiCalculatorDestination(onBackClick)`. Entry point:
  `NavController.navigateToEmiCalculator()`.
- **DI:** `EmiCalculatorModule`.

See `FEATURE_AUTHORING.md` § "Pure-compute features (no store)" — this module is one of its
two canonical examples.
