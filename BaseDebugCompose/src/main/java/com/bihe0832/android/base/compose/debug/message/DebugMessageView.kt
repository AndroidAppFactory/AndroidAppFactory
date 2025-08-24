package com.bihe0832.android.base.compose.debug.message

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.bihe0832.android.app.message.AAFMessageListFragment
import com.bihe0832.android.app.message.AAFMessageManager
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.common.compose.debug.DebugUtilsV2
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.message.data.MessageInfoItem
import com.bihe0832.android.lib.gson.JsonHelper
import com.bihe0832.android.lib.log.ZLog


@Composable
fun DebugMessageComposeView() {
    val activity = LocalContext.current as? Activity
    DebugItem("初始化并拉取公告") { testTrace() }
    DebugItem("打开消息详情页(Fragment)") {
        DebugUtilsV2.startComposeActivity(it, "打开消息详情页(Fragment)", AAFMessageListFragment::class.java.name)
    }
    DebugItem("打开消息详情页(Activity)") {
        RouterHelper.openPageByRouter(RouterConstants.MODULE_NAME_MESSAGE)
    }
    DebugItem("测试消息样式") {
        activity?.let {
            testMsg(it)
        }
    }
}

private fun testTrace() {
    AAFMessageManager.updateMsg()
}

private fun testMsg(activity: Activity) {
    val messageData = "{" + "        \"id\": \"1\"," + "        \"type\": \"text\"," + "        \"title\": \"置顶应用下载公告带通知栏通知\"," + "        \"content\": \"1. 修复下载期间偶现Crash，提升稳定性 <BR> 2. 优化下载及使用用户体验\"," + "        \"should_top\": \"1\"," + "        \"expire_date\": \"-1\"," + "        \"action\": \"https://android.bihe0832.com/doc/summary/links.html\"," + "        \"isNotify\": \"1\"," + "        \"showFace\": 2," + "        \"create_date\": \"202307200600\"" + "    }"
    ZLog.d(JsonHelper.toJson(messageData).toString())
    JsonHelper.fromJson(messageData, MessageInfoItem::class.java)?.let {
        AAFMessageManager.showMessage(activity, it, false)
    }

}
