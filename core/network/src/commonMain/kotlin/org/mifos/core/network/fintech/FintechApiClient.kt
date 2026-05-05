/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.network.fintech

import de.jensklingenberg.ktorfit.Ktorfit

class FintechApiClient(
    frankfurterKtorfit: Ktorfit,
    coinGeckoKtorfit: Ktorfit,
) {
    val frankfurterApi: FrankfurterApi by lazy { frankfurterKtorfit.createFrankfurterApi() }
    val coinGeckoApi: CoinGeckoApi by lazy { coinGeckoKtorfit.createCoinGeckoApi() }
}
