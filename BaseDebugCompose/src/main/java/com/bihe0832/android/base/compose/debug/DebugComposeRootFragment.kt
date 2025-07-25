package com.bihe0832.android.base.compose.debug

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bihe0832.android.common.compose.common.fragment.CommonComposeFragment
import com.bihe0832.android.common.compose.debug.item.DebugComposeItem
import com.bihe0832.android.common.compose.state.RenderState

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/5.
 * Description: Description
 *
 */

open class DebugComposeRootFragment : CommonComposeFragment() {

    @Composable
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
    Column {
        DebugComposeItem("Compose 公共调试（语言、主题）", "CommonConfigView")
        DebugComposeItem("Compose List 调试", "CommonListView")
    }
}


