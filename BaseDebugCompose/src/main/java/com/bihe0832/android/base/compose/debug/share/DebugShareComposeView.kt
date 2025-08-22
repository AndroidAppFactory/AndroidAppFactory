package com.bihe0832.android.base.compose.debug.share

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bihe0832.android.common.compose.debug.item.DebugComposeActivityItem
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.common.share.ShareAPPActivity

@Preview
@Composable
fun DebugShareComposeView() {
    DebugContent {
        DebugComposeActivityItem("分享APK", ShareAPPActivity::class.java)
        DebugComposeActivityItem("底部分享Activity", DebugBottomActivity::class.java)
    }


}
