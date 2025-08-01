package com.bihe0832.android.base.compose.debug

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bihe0832.android.base.compose.debug.common.DebugComposeConfigView
import com.bihe0832.android.base.compose.debug.list.DebugComposeListView
import com.bihe0832.android.common.compose.base.BaseComposeFragment
import com.bihe0832.android.common.compose.debug.DebugContent
import com.bihe0832.android.common.compose.debug.item.DebugComposeItem
import com.bihe0832.android.common.compose.debug.module.device.storage.DebugCurrentStorageActivity
import com.bihe0832.android.common.compose.state.RenderState

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/5.
 * Description: Description
 *
 */

open class DebugComposeRootFragment : BaseComposeFragment() {

    override fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                DebugRootView()
            }
        }
    }
}

@Preview
@Composable
fun DebugRootView() {
    DebugContent {
        DebugComposeItem(
            "Compose 公共调试（语言、主题）", "DebugComposeConfigView"
        ) { DebugComposeConfigView() }
        DebugComposeItem("Compose List 调试", "DebugComposeListView") { DebugComposeListView() }
        DebugComposeItem(
            "Compose List 调试",
            "DebugComposeListView"
        ) { DebugCurrentStorageActivity() }
    }
}


