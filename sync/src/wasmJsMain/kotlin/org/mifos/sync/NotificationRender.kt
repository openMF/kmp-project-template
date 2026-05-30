// File: samples/kmp-project-template/sync/src/wasmJsMain/kotlin/org/mifos/sync/NotificationRender.kt
package org.mifos.sync
actual fun renderNotification(content: NotificationContent) {
    println("[wasmJs-stub-render] ${content.title} — ${content.body}")
}
