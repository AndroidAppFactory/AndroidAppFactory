package com.bihe0832.android.common.compose.common.fragment

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.bihe0832.android.common.compose.base.BaseComposeFragment
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.common.compose.ui.EmptyView
import java.util.Locale

/**
 * @author zixie code@bihe0832.com Created on 4/9/22.
 *
 * 一个空白的Fragment，用于一些便捷调试的场景
 */
open class CommonComposeFragment : BaseComposeFragment() {

    @Composable
    override fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                EmptyView(message = "空白页面", colorP = Color.White)
            }
        }
    }

    @Preview
    @Composable
    open fun FragmentContentRenderPreview() {
        getContentRender().Content()
    }
}