# `feature/settings/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 74 tracked files — 30× `.kt`, 20× `.xml`, 20× `.png`, 1× `.gitignore`, 1× `.md`

**30 Kotlin files** — 3 screen(s), 3 ViewModel(s).

Screens: `NotificationScreen`, `SettingsScreen`, `SyncAndDraftsScreen`

ViewModels: `ConflictInboxViewModel`, `SettingsViewModel`, `SyncAndDraftsViewModel`

**Consumes:** `core/store/user`

### Docs in the tree

| Path | |
|---|---|
| `feature/settings/README.md` | :feature:settings |

<!-- tree-scaffold:end -->
## Significance

The largest feature, and the one that touches the most cross-cutting state: theme, language, dynamic
colour, screen capture, app lock, the conflict inbox and the sync-and-drafts view.

Two of those are worth calling out. The **conflict inbox** is where a MUTABLE store's divergences
surface for a human to resolve — without it, a server-wins resolution silently discards a user's edit.
The **sync-and-drafts** screen makes the offline write queue visible, which is the difference between
"the app lost my change" and "the app is holding my change until it can send it".

`UserEditableSettings` is deliberately narrower than `UserData`, so a settings screen cannot write a
session or lock field it has no business touching.
