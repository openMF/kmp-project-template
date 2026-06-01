/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.infra

import de.jensklingenberg.ktorfit.Ktorfit
import kpt.core.network.crypto.api.CoinGeckoApi
import kpt.core.network.crypto.api.createCoinGeckoApi
import kpt.core.network.currency.api.FrankfurterApi
import kpt.core.network.currency.api.createFrankfurterApi
import kpt.core.network.economic.api.FredApi
import kpt.core.network.economic.api.WorldBankApi
import kpt.core.network.economic.api.createFredApi
import kpt.core.network.economic.api.createWorldBankApi

/** Multi-domain API aggregator. Consumer apps replace this with their own client. */
class FintechApiClient(
    frankfurterKtorfit: Ktorfit,
    coinGeckoKtorfit: Ktorfit,
    fredKtorfit: Ktorfit,
    worldBankKtorfit: Ktorfit,
) {
    val frankfurterApi: FrankfurterApi by lazy { frankfurterKtorfit.createFrankfurterApi() }
    val coinGeckoApi: CoinGeckoApi by lazy { coinGeckoKtorfit.createCoinGeckoApi() }
    val fredApi: FredApi by lazy { fredKtorfit.createFredApi() }
    val worldBankApi: WorldBankApi by lazy { worldBankKtorfit.createWorldBankApi() }
}
