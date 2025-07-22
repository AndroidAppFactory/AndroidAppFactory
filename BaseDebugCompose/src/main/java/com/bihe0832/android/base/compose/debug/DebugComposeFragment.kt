package com.bihe0832.android.base.compose.debug

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.bihe0832.android.common.compose.common.fragment.CommonComposeFragment
import com.bihe0832.android.common.compose.debug.item.DebugComposeItem
import com.bihe0832.android.common.compose.state.RenderState
import java.util.Locale

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/5.
 * Description: Description
 *
 */
@Preview
@Composable
fun DebugComposeView() {
    val context = LocalContext.current
    Column {
        DebugComposeItem("Compose 公共调试（语言、主题）", "CommonConfigView")
        DebugComposeItem("Compose List 调试", "CommonListView")
    }
}


open class DebugComposeFragment : CommonComposeFragment() {

    @Composable
    override fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content(currentLanguage: Locale) {
                DebugComposeView()
            }
        }
    }

}