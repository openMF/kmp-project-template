/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.saveable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.lifecycle.Lifecycle
import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kpt.core.base.ui.effects.LivecycleEventEffect

/**
 * App-root [SaveableStateRegistry] that persists saveable values to
 * multiplatform-settings so `rememberSaveable(...)` (including the built-in
 * `rememberLazyListState()` / `rememberScrollState()` savers) survives full
 * process death on every target — not just Android, where the framework's
 * own `saveInstanceState` already handles it.
 *
 * Wired ONCE, at the app root, in [cmp.navigation.ComposeApp] via
 * `CompositionLocalProvider(LocalSaveableStateRegistry provides <this>)`.
 * Feature modules carry zero retention code — they use plain
 * `rememberSaveable { ... }` / `rememberLazyListState()` / `rememberScrollState()`
 * and inherit the persisted registry from the composition local.
 *
 * ## Persistence model
 *
 *  - **Restore** on first composition: read a JSON blob keyed by [storageKey]
 *    from [settings], decode it into a `Map<String, List<Any?>>`, and hand
 *    it to the underlying registry as `restoredValues`.
 *  - **Persist** on `Lifecycle.Event.ON_STOP` / `ON_PAUSE` / `ON_RESUME` and
 *    on Compose dispose: call [SaveableStateRegistry.performSave], filter to
 *    JSON-serializable primitives, encode, and write back to [settings].
 *
 * ## What is saved
 *
 * The `canBeSaved` predicate restricts persisted values to primitives
 * (`Int`, `Long`, `Float`, `Double`, `Boolean`, `String`). The built-in
 * `LazyListState.Saver` / `ScrollState.Saver` save `Int`s — those qualify.
 * Complex saveable values (custom savers producing lists of nested objects,
 * etc.) are dropped by the predicate; they still survive in-session via the
 * default composition-registry semantics, they just do not survive process
 * death without a matching primitive representation.
 */
@Composable
fun rememberPersistentSaveableStateRegistry(
    settings: Settings,
    storageKey: String = DEFAULT_STORAGE_KEY,
): SaveableStateRegistry {
    val restored = remember(settings, storageKey) {
        readRestoredValues(settings, storageKey)
    }
    val registry = remember(settings, storageKey, restored) {
        SaveableStateRegistry(restoredValues = restored, canBeSaved = ::isPrimitiveSaveable)
    }

    LivecycleEventEffect { _, event ->
        if (event == Lifecycle.Event.ON_STOP ||
            event == Lifecycle.Event.ON_PAUSE ||
            event == Lifecycle.Event.ON_RESUME
        ) {
            persistRegistry(settings, storageKey, registry)
        }
    }

    DisposableEffect(registry) {
        onDispose {
            persistRegistry(settings, storageKey, registry)
        }
    }

    return registry
}

/** Storage key used by [rememberPersistentSaveableStateRegistry] when the caller does not override it. */
const val DEFAULT_STORAGE_KEY: String = "app.saveable.state"

/** Predicate used as [SaveableStateRegistry.canBeSaved]. Restricts to JSON-serializable primitives. */
internal fun isPrimitiveSaveable(value: Any?): Boolean =
    value is Int ||
        value is Long ||
        value is Float ||
        value is Double ||
        value is Boolean ||
        value is String

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = false
}

/** Read [storageKey] from [settings], decode to a map, return `null` on absent/malformed. */
internal fun readRestoredValues(
    settings: Settings,
    storageKey: String,
): Map<String, List<Any?>>? {
    val raw = settings.getStringOrNull(storageKey) ?: return null
    val root = runCatching { json.parseToJsonElement(raw) as? JsonObject }.getOrNull()
    return root?.let { decodeSaveableMap(it) }
}

/** Snapshot the registry, encode, write to [settings]. Best-effort — swallows any encoding error. */
internal fun persistRegistry(
    settings: Settings,
    storageKey: String,
    registry: SaveableStateRegistry,
) {
    val snapshot = runCatching { registry.performSave() }.getOrNull() ?: return
    val encoded = runCatching {
        json.encodeToString(JsonObject.serializer(), encodeSaveableMap(snapshot))
    }.getOrNull()
    if (encoded != null) settings.putString(storageKey, encoded)
}

/** Encode a `Map<String, List<Any?>>` as a JsonObject of JsonArrays of JsonPrimitives. */
internal fun encodeSaveableMap(source: Map<String, List<Any?>>): JsonObject = buildJsonObject {
    for ((key, values) in source) {
        put(key, encodeValueList(values))
    }
}

private fun encodeValueList(values: List<Any?>): JsonArray = buildJsonArray {
    for (value in values) {
        val primitive = encodePrimitive(value) ?: continue
        add(primitive)
    }
}

private fun encodePrimitive(value: Any?): JsonPrimitive? = when (value) {
    is Int -> JsonPrimitive(value)
    is Long -> JsonPrimitive(value)
    is Float -> JsonPrimitive(value)
    is Double -> JsonPrimitive(value)
    is Boolean -> JsonPrimitive(value)
    is String -> JsonPrimitive(value)
    else -> null
}

/** Decode a JsonObject shaped by [encodeSaveableMap] back into a `Map<String, List<Any?>>`. */
internal fun decodeSaveableMap(root: JsonObject): Map<String, List<Any?>> {
    val out = mutableMapOf<String, List<Any?>>()
    for ((key, element) in root) {
        val list = decodeValueList(element) ?: continue
        out[key] = list
    }
    return out
}

private fun decodeValueList(element: JsonElement): List<Any?>? {
    val array = runCatching { element.jsonArray }.getOrNull() ?: return null
    return array.map { decodePrimitive(it) }
}

private fun decodePrimitive(element: JsonElement): Any? {
    val prim = runCatching { element.jsonPrimitive }.getOrNull() ?: return null
    return typeSniffPrimitive(prim)
}

private fun typeSniffPrimitive(prim: JsonPrimitive): Any? {
    if (prim.isString) return prim.content
    return prim.booleanOrNull
        ?: prim.intOrNull
        ?: prim.longOrNull
        ?: prim.doubleOrNull
        ?: prim.content
}
