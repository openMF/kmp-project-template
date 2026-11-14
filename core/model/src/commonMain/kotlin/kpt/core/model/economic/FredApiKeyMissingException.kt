/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.economic

/**
 * The build has no FRED developer key, so no interest-rate series can be fetched.
 *
 * A setup gap, not a failure: retrying cannot succeed, so screens render it as an explicit
 * "not configured" notice rather than a generic error with a retry button. The key comes from
 * `FRED_API_KEY` (env var or `local.properties`) at build time — see `FredApiConfig`.
 */
class FredApiKeyMissingException : IllegalStateException(
    "FRED_API_KEY is not configured — supply a key via FredApiConfig before requesting interest-rate data.",
)
