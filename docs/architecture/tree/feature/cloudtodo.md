# `feature/cloudtodo/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 30 tracked files — 20× `.xml`, 9× `.kt`, 1× `.kts`

**9 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `CloudTodoScreen`

ViewModels: `CloudTodoViewModel`

**Consumes:** `core/store/cloudtodo`

**Store5 archetype(s) exercised:** `MUTABLE` — from `core/store/STORE_ARCHETYPES.yaml`, which product-health fails the build against when an archetype loses its last showcase.

<!-- tree-scaffold:end -->
## Significance

The **MUTABLE Store5 archetype**, wired end to end and the only feature that writes to a server.

Reads stream from Room; toggling `completed` writes through the Store5 `Updater` (`PUT /todos/{id}`);
a write that fails offline is recorded by the RoomBookkeeper and retried on reconnect. It exists to
prove that path works, which is why it is deliberately trivial in every other respect — one boolean on
one entity. A richer demo would hide the mechanism it is meant to demonstrate.
