/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.network.infra

import de.jensklingenberg.ktorfit.Ktorfit
import org.mifos.core.network.crypto.api.CoinGeckoApi
import org.mifos.core.network.crypto.api.createCoinGeckoApi
import org.mifos.core.network.currency.api.FrankfurterApi
import org.mifos.core.network.currency.api.createFrankfurterApi
import org.mifos.core.network.economic.api.FredApi
import org.mifos.core.network.economic.api.WorldBankApi
import org.mifos.core.network.economic.api.createFredApi
import org.mifos.core.network.economic.api.createWorldBankApi

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
