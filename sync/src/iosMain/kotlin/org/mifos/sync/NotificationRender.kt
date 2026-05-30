// File: samples/kmp-project-template/sync/src/iosMain/kotlin/org/mifos/sync/NotificationRender.kt
package org.mifos.sync
actual fun renderNotification(content: NotificationContent) {
    println("[ios-stub-render] ${content.title} — ${content.body} (UNUserNotificationCenter wiring is a follow-up)")
}
