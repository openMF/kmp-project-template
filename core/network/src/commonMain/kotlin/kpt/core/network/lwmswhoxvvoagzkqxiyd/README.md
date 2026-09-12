# `project` — Supabase access-point package

SCAFFOLDED by `./gradlew syncForkConfig` from the `project` access point in
`app-profile/app.yaml#network.access_points`. One package per endpoint.

## Layout: `{supabase-project}/{table}/{api,dto}`

```
project/                      ← the access-point id == the Supabase project ref
  app_config/                 ← one package per TABLE
    api/
      AppConfigApi.kt         ← interface — the contract, no Supabase types
      AppConfigApiImpl.kt     ← @ApiBinding("lwmswhoxvvoagzkqxiyd") — the postgrest facade
    dto/
      RemoteAppConfigDto.kt   ← the wire types for THIS table
```

**`project` is a placeholder the fork renames.** The template cannot ship a real Supabase project, so
the access point id, the `base_url` host and this package are all the neutral word `project`. A fork
points them at its own project — the id becomes the project ref (the public identity in
`https://<ref>.supabase.co`, e.g. `azcxfedokrtlsyxueedn`), and the package is renamed to match. Rename
BOTH together: the annotation argument is matched against the declared access-point ids, so a package
rename alone leaves `@ApiBinding` naming an id app-profile no longer declares, which `network-ksp`
rejects at compile time (and NAP-4 catches in CI).

**One package per table, not one class per project.** A project with five tables has five API
interfaces, each with its own DTOs, rather than one class that grows without bound and drags every
table's wire types into every consumer.

## Interface + impl

`api/` holds BOTH, and the split is load-bearing:

- the **interface** is what consumers inject and what a test fakes — no `SupabaseConfigClient` needed
- the **impl** carries `@ApiBinding` (it is what gets constructed) and takes a single
  `SupabaseConfigClient` constructor argument, which is the whole contract `supabaseApi<T>` requires

The generated binding is `supabaseApi<AppConfigApi>("project") { AppConfigApiImpl(it) }` — the
processor resolves the impl's single supertype and binds THAT. Without the explicit type argument
`single<T>` would infer the impl and every `get<AppConfigApi>()` would miss at runtime, after
compiling cleanly. Give each API type exactly one interface; `network-ksp` errors on more than one
rather than guessing which to bind.

REST points are interfaces too — there Ktorfit generates the implementation, so only the interface is
written. The seam is identical on both sides; a caller should never be able to tell.

## Inert by default

The template declares the endpoint but ships no project and no anon key, so
`SupabaseConfigClient.isConfigured` is false and every call returns empty rather than throwing — an
unconfigured fork stays on the "no remote config, use defaults" path instead of crashing at start-up.
Construction never touches the network.

## Adding a table

1. `mkdir -p project/<table>/{api,dto}`
2. write the DTOs, then the interface, then the impl annotated `@ApiBinding("<access-point-id>")`
3. build — the Koin binding is generated; there is no wiring step

Delete this package by removing its access point from app-profile.
