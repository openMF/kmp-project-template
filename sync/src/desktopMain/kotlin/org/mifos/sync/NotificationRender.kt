// File: samples/kmp-project-template/sync/src/desktopMain/kotlin/org/mifos/sync/NotificationRender.kt
package org.mifos.sync
actual fun renderNotification(content: NotificationContent) {
    println("[desktop-stub-render] ${content.title} — ${content.body}")
}
