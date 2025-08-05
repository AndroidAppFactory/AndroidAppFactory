package com.bihe0832.android.base.compose.debug

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bihe0832.android.base.compose.debug.common.DebugComposeConfigView
import com.bihe0832.android.base.compose.debug.list.DebugComposeListView
import com.bihe0832.android.common.compose.debug.DebugUtilsV2
import com.bihe0832.android.common.compose.debug.item.DebugComposeItem
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.module.device.storage.DebugCurrentStorageActivity
import com.bihe0832.android.common.compose.debug.ui.DebugContent

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/5.
 * Description: Description
 *
 */

@Preview
@Composable
fun DebugComposeModuleView() {


    DebugContent {
        DebugComposeItem(
            "Compose 公共调试（语言、主题）", "DebugComposeConfigView"
        ) { DebugComposeConfigView() }

        DebugComposeItem("Compose List 调试", "DebugComposeListView") { DebugComposeListView() }
        DebugItem(
            "Compose List 调试",
        ) { context ->
            DebugUtilsV2.startActivityWithException(
                context,
                DebugCurrentStorageActivity::class.java
            )
        }
    }
}


