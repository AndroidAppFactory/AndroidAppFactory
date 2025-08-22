package com.bihe0832.android.base.compose.debug.toast

import android.graphics.Color
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bihe0832.android.base.compose.debug.R
import com.bihe0832.android.common.compose.debug.item.DebugComposeActivityItem
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.toast.ToastUtil
import com.bihe0832.android.lib.utils.intent.IntentUtils

@Preview
@Composable
fun DebugToastComposeView() {
    DebugContent {
        DebugItem("Toast测试") {
            ToastUtil.showTop(
                it,
                "这是一个测试用的<font color ='#38ADFF'><b>测试消息</b></font>",
                Toast.LENGTH_LONG
            )
        }
        DebugItem("普通Toast") { ToastUtil.showShort(it, "这是一个普通Toast") }
        DebugItem("顶部Toast") {
            ToastUtil.showTop(it, "这是一个顶部Toast", Toast.LENGTH_SHORT)
        }
        DebugItem("Tips Toast") {
            ToastUtil.showTips(
                it, it.resources.getDrawable(R.mipmap.icon).apply {
                    setTint(Color.RED)
                }, "执行成功", Toast.LENGTH_SHORT
            )
        }
        DebugItem("调试版本") { ZixieContext.showDebugEditionToast() }
        DebugItem("敬请期待") { ZixieContext.showWaiting() }
        DebugComposeActivityItem("打开Activity Toast 立即关闭", DebugToastActivity::class.java)

        DebugItem("仅前台Toast") {
            ZixieContext.showLongToastJustAPPFront("这是一个仅前台Toast")
            ThreadManager.getInstance().start({
                IntentUtils.goHomePage(it)
            }, 3)
            ThreadManager.getInstance().start({
                ZixieContext.showLongToastJustAPPFront("这是一个仅前台Toast")
            }, 5)
        }
        DebugItem("不分前后台Toast") {
            ZixieContext.showToast("这是一个不分前后台Toast")


            ThreadManager.getInstance().start({
                ZixieContext.showToast("这是一个不分前后台Toast")
            }, 5)

            ThreadManager.getInstance().start({
                IntentUtils.goHomePage(it)
            }, 3)
        }
    }
}
