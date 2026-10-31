-- app_server_config — read-only for app clients, locked at the GRANT level as well as by RLS.
--
-- The app reads the server-instance list with the PUBLISHABLE key (role `anon`), and that key ships
-- inside every APK/IPA, so anyone can extract it. Whatever `anon` may do, everyone may do. For this
-- table that must be: read enabled rows, nothing else.
--
-- RLS alone is not enough: Supabase's default privileges grant INSERT/UPDATE/DELETE/TRUNCATE on public
-- tables to anon and authenticated, so a table protected only by RLS is one `disable row level
-- security` away from being writable by the public key. This migration revokes those grants, forces
-- RLS (also for the owner role), and keeps exactly one client policy: SELECT where enabled.
-- Writes stay with the owner (dashboard / SQL editor) or the server-only secret key.
--
-- Template-safe and idempotent: a fork without this table is a no-op; re-running changes nothing.

do $$
begin
  if to_regclass('public.app_server_config') is null then
    raise notice 'app_server_config not present — nothing to lock down';
    return;
  end if;

  execute 'alter table public.app_server_config enable row level security';
  execute 'alter table public.app_server_config force row level security';

  execute 'revoke insert, update, delete, truncate, references, trigger on public.app_server_config from anon, authenticated';
  execute 'grant select on public.app_server_config to anon, authenticated';

  execute 'drop policy if exists "app_server_config is publicly readable" on public.app_server_config';
  execute 'create policy "app_server_config is publicly readable" on public.app_server_config for select to anon, authenticated using (enabled)';

  -- The updated_at trigger function is not an API.
  if to_regprocedure('public.app_server_config_touch()') is not null then
    execute 'revoke execute on function public.app_server_config_touch() from public, anon, authenticated';
  end if;
end $$;

-- Verify after applying — expect rls=t forced=t, one SELECT policy, anon/authenticated = SELECT only:
--   select relrowsecurity as rls, relforcerowsecurity as forced from pg_class where oid = 'public.app_server_config'::regclass;
--   select policyname, cmd, roles from pg_policies where schemaname = 'public' and tablename = 'app_server_config';
--   select grantee, string_agg(privilege_type, ',') from information_schema.role_table_grants
--    where table_schema = 'public' and table_name = 'app_server_config' and grantee in ('anon','authenticated') group by 1;
