// File: samples/kmp-project-template/sync/src/commonMain/kotlin/org/mifos/sync/SyncWorkHelpers.kt
package org.mifos.sync

import io.github.mobilebytelabs.worker.Constraints
import io.github.mobilebytelabs.worker.NetworkType

const val SYNC_WORK_NAME = "SyncWork"
const val NOTIFICATION_WORK_PREFIX = "NotificationWork"
const val FOREGROUND_NOTIFICATION_ID_SYNC = 9_001
const val FOREGROUND_NOTIFICATION_ID_NOTIFICATION = 9_002

val SyncConstraints: Constraints = Constraints {
    setRequiredNetworkType(NetworkType.CONNECTED)
}
