// File: samples/kmp-project-template/sync/src/iosMain/kotlin/org/mifos/sync/ExactAlarmScheduler.kt
package org.mifos.sync
// iOS: no native exact-alarm API in v1. scheduleDataSyncAtExact delegates to
// scheduleDataSyncAt (flex-window) via the commonMain DefaultWorkScheduler default.
// UNUserNotificationCenter exact triggers are a follow-up.
