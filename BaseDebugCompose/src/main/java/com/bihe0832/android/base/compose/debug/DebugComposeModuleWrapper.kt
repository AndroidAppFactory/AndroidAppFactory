package com.bihe0832.android.base.compose.debug

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.bihe0832.android.base.compose.debug.common.DebugComposeConfigView
import com.bihe0832.android.base.compose.debug.language.DebugComposeLanguageView
import com.bihe0832.android.base.compose.debug.list.DebugComposeListView

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/5.
 * Description: Description
 *
 */


@Preview(showSystemUi = true, device = Devices.PIXEL_4)
@Composable
fun DebugComposeViewPreview() {
    GetCommonDebug(DebugComposeCore.DebugCommonCompose + "LanguageView")
}

@Composable
fun GetCommonDebug(viewKey: String): Boolean {
    when (viewKey) {
        DebugComposeCore.DebugCommonCompose + "ComposeView" -> {
            DebugRootView()
            return true
        }

        DebugComposeCore.DebugCommonCompose + "ConfigView" -> {
            DebugComposeConfigView()
            return true
        }

        DebugComposeCore.DebugCommonCompose + "LanguageView" -> {
            DebugComposeLanguageView()
            return true
        }

        DebugComposeCore.DebugCommonCompose + "ListView" -> {
            DebugComposeListView()
            return true
        }
    }
    return false
}

