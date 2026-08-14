# :feature:showcase

Dev-only galleries — not a product feature. Exhibits framework visuals rather than app data;
gate the entry points behind debug builds.

- **State Gallery:** `StateGalleryScreen` — every `ScreenState` visual (loading skeleton,
  row shimmer, empty, error chip, inline error pill, content) rendered side-by-side from
  `LocalScreenStateDefaults`. Route: `StateGalleryRoute`, wired via `stateGalleryGraph`.
- **Transition Gallery:** `TransitionGalleryScreen` — a tile per transition factory; tapping
  navigates to a demo destination (`TransitionDemoScreen`) that auto-pops after 2s, a visual
  smoke test for the duration-symmetry invariants in `SharedAxisSpec` / `TransitionPushSpec`.
  Routes: `TransitionGalleryRoute`, `TransitionDemoRoute(variantName)`, wired via
  `transitionGalleryGraph`.
- No ViewModels and no `*Module.kt` — everything here is stateless UI, no DI required.

See `FEATURE_AUTHORING.md` for the archetypes these visuals are drawn from.
