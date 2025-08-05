package com.bihe0832.android.common.compose.debug.module.router

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.bihe0832.android.common.compose.debug.DebugBaseComposeActivity
import com.bihe0832.android.common.compose.debug.DebugUtilsV2
import com.bihe0832.android.common.compose.debug.item.DebugTips
import com.bihe0832.android.common.compose.debug.item.RouterItem
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.framework.router.RouterInterrupt

open class DebugRouterComposeActivity : DebugBaseComposeActivity() {


    open fun getRouterList(): List<String> {
        return mutableListOf<String>().apply {
            for (i in 1..10) {
                add(RouterInterrupt.getRouterLogPath())
            }
        }
    }

    override fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                GetRouterView(getRouterList())
            }
        }
    }


}

@Composable
fun GetRouterView(router: List<String>) {
    val context = LocalContext.current
    DebugContent {
        DebugTips("可以在PC打开下面链接生成二维码后测试：<small>https://microdemo.bihe0832.com/MyJS/router/</small> ") {
            DebugUtilsV2.showInfo(
                context,
                "路由测试工具链接分享",
                "路由测试工具链接：\n https://microdemo.bihe0832.com/MyJS/router/"
            )
        }
        router.forEach {
            RouterItem(it)
        }
    }
}