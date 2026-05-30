// File: samples/kmp-project-template/sync/src/commonMain/kotlin/org/mifos/sync/NotificationRender.kt
package org.mifos.sync

/** Platform-specific notification rendering. v1 actuals: Android real; iOS/Desktop/Web stubs. */
expect fun renderNotification(content: NotificationContent)
