/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.analytics.di

import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import org.koin.core.module.Module
import org.koin.dsl.module
import template.core.base.analytics.AnalyticsHelper
import template.core.base.analytics.BuildKonfig
import template.core.base.analytics.StubAnalyticsHelper

actual val analyticsModule: Module = module {
    single<AnalyticsHelper> {
        StubAnalyticsHelper()
//        // The "Switch" happens here in Kotlin code, not Gradle source sets
//        if (BuildKonfig.IS_DEMO_MODE) {
//            // The "Demo" version
//        } else {
//            FirebaseAnalyticsHelper() // The "Prod" version
//        }
    }

//    single<FirebaseAnalytics> { Firebase.analytics }
}
