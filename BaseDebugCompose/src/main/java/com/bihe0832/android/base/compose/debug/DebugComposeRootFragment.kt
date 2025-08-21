package com.bihe0832.android.base.compose.debug

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bihe0832.android.base.compose.debug.cache.DebugComposeCacheView
import com.bihe0832.android.base.compose.debug.clipboard.DebugComposeClipboardView
import com.bihe0832.android.base.compose.debug.common.DebugComposeConfigView
import com.bihe0832.android.base.compose.debug.convert.DebugComposeConvertView
import com.bihe0832.android.base.compose.debug.list.DebugComposeListView
import com.bihe0832.android.base.compose.debug.module.dialog.DebugComposeDialogView
import com.bihe0832.android.common.compose.debug.DebugUtilsV2
import com.bihe0832.android.common.compose.debug.item.DebugComposeItem
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.module.audio.DebugAudioListActivity
import com.bihe0832.android.common.compose.debug.module.audio.DebugAudioListWithProcessActivity
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
fun AAFDebugModuleView() {

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
        DebugComposeItem("Dialog 调试", "DebugComposeDialogView") { DebugComposeDialogView() }
        DebugComposeItem("Cache、数据中心测试", "DebugComposeCacheView") { DebugComposeCacheView() }
        DebugComposeItem("剪切板调试", "DebugComposeClipboardView") { DebugComposeClipboardView() }
        DebugComposeItem("剪切板调试", "DebugComposeClipboardView") { DebugComposeClipboardView() }
        DebugComposeItem("数据转化", "DebugComposeConvertView") { DebugComposeConvertView() }

        DebugItem(
            "本地 WAV 查看",
        ) { context ->
            DebugUtilsV2.startActivityWithException(
                context,
                DebugAudioListActivity::class.java
            )
        }
        DebugItem(
            "本地 WAV 查看2",
        ) { context ->
            DebugUtilsV2.startActivityWithException(
                context,
                DebugAudioListWithProcessActivity::class.java
            )
        }
    }
}


