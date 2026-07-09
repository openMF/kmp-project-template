/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.config

import kpt.core.base.network.SupabaseCredentials
import kpt.core.network.BuildKonfig

/**
 * Default [SupabaseCredentials] for the toolkit, sourced from BuildKonfig
 * (`SUPABASE_URL` / `SUPABASE_ANON_KEY`, populated from `local.properties` or env vars).
 *
 * The template ships **no** Supabase project, so both values are empty by default and
 * [SupabaseCredentials.isConfigured] returns `false` — [kpt.core.base.network.SupabaseConfigClient]
 * stays inert until a fork provides real values. Forks either set the two `local.properties`
 * keys, or override the `single<SupabaseCredentials>` binding in their app module.
 */
data class BuildKonfigSupabaseCredentials(
    override val url: String = BuildKonfig.SUPABASE_URL,
    override val anonKey: String = BuildKonfig.SUPABASE_ANON_KEY,
) : SupabaseCredentials
