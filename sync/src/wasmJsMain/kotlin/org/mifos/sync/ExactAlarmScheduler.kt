// File: samples/kmp-project-template/sync/src/wasmJsMain/kotlin/org/mifos/sync/ExactAlarmScheduler.kt
package org.mifos.sync
// wasmJs: no native exact-alarm API in v1. scheduleDataSyncAtExact delegates to
// scheduleDataSyncAt (flex-window) via the commonMain DefaultWorkScheduler default.
