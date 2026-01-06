package org.mifos.core.data.util

import android.R.attr.autoStart
import dev.jordond.connectivity.Connectivity

actual val connectivityProvider: Connectivity
    get() = Connectivity {
        autoStart(true)
    }