/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.designsystem.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * A composable wrapper around Material3 [ModalBottomSheet] that provides a consistent, theme-aware
 * bottom sheet implementation for the CMP design system.
 *
 * This function automatically manages internal visibility state and provides a `hideSheet` callback
 * to dismiss the sheet programmatically from within [sheetContent].
 *
 * @param onDismiss Callback invoked when the bottom sheet is dismissed.
 * @param modifier Modifier applied to the [ModalBottomSheet]. *(Default: [Modifier])*
 * @param sheetState The state object to control and observe the bottom sheet's visibility and interaction.
 * (Default: [rememberModalBottomSheetState()])*
 * @param sheetMaxWidth The maximum width of the sheet. *(Default: [BottomSheetDefaults.SheetMaxWidth])*
 * @param shape The shape of the bottom sheet container. *(Default: [BottomSheetDefaults.ExpandedShape])*
 * @param containerColor Background color of the sheet container. *(Default: [BottomSheetDefaults.ContainerColor])*
 * @param contentColor Foreground content color inside the sheet. *(Default: [contentColorFor(containerColor)])*
 * @param tonalElevation Elevation applied to the sheet. *(Default: 0.dp)*
 * @param scrimColor Color of the background scrim behind the sheet.
 *(Default: [BottomSheetDefaults.ScrimColor])*
 * @param dragHandle Optional composable to show a drag handle.
 * (Optional, Default: [BottomSheetDefaults.DragHandle()])*
 * @param contentWindowInsets Insets to be applied to the sheet’s content.
 * (Default: [BottomSheetDefaults.windowInsets])*
 * @param properties Additional platform-specific configuration options.
 * (Default: [ModalBottomSheetDefaults.properties])*
 * @param sheetContent The content of the bottom sheet.
 * This lambda receives a `hideSheet()` callback that should be used to programmatically hide the sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CMPBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    sheetMaxWidth: Dp = BottomSheetDefaults.SheetMaxWidth,
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    containerColor: Color = BottomSheetDefaults.ContainerColor,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = 0.dp,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    contentWindowInsets: @Composable () -> WindowInsets = { BottomSheetDefaults.windowInsets },
    properties: ModalBottomSheetProperties = ModalBottomSheetDefaults.properties,
    sheetContent: @Composable ColumnScope.(hideSheet: () -> Unit) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(true) }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                onDismiss()
            },
            sheetState = sheetState,
            sheetMaxWidth = sheetMaxWidth,
            shape = shape,
            containerColor = containerColor,
            contentColor = contentColor,
            tonalElevation = tonalElevation,
            scrimColor = scrimColor,
            dragHandle = dragHandle,
            contentWindowInsets = contentWindowInsets,
            properties = properties,
            modifier = modifier,
        ) {
            sheetContent {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        showBottomSheet = false
                        onDismiss()
                    }
                }
            }
        }
    }
}
