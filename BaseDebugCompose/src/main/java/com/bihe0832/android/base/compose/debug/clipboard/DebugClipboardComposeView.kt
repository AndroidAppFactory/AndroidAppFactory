package com.bihe0832.android.base.compose.debug.clipboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.bihe0832.android.app.tools.AAFTools
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.text.ClipboardUtil


@Preview
@Composable
fun DebugComposeClipboardViewPreview() {
    DebugClipboardComposeView()
}

@Composable
fun DebugClipboardComposeView() {
    val context = LocalContext.current
    DebugContent {
        DebugItem("复制到剪切板") {
            ClipboardUtil.copyToClipboard(context, "this is a test")
        }
        DebugItem("读取剪切板数据") {
            ZixieContext.showToast(AAFTools.pasteFromClipboard(context))
        }
        DebugItem("清空剪切板") {
            ClipboardUtil.clearClipboard(context)
        }
    }
}

