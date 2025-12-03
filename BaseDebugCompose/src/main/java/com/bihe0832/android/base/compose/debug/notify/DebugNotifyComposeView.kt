package com.bihe0832.android.base.compose.debug.notify

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.common.permission.AAFPermissionManager
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.notification.DownloadNotifyManager
import com.bihe0832.android.lib.notification.NotifyManager
import com.bihe0832.android.lib.notification.RefreshNotifyManager
import com.bihe0832.android.lib.timer.BaseTask
import com.bihe0832.android.lib.timer.TaskManager

private const val LOG_TAG = "DebugNotifyComposeView"

@Preview
@Composable
fun DebugNotifyComposeView() {
    DebugContent {
        DebugItem("是否开启通知") { AAFPermissionManager.hasNotifyPermission() }
        DebugItem("打开通知设置") { AAFPermissionManager.openNotifyPermission() }
        DebugItem("普通通知") { testNotify(it) }
        DebugItem("可刷新通知") { testRefreshNotify(it) }
        DebugItem("下载通知") { testNotifyProcess(it) }
    }
}

internal var content = "这是一个测试"
internal var process = 0
internal fun testRefreshNotify(context: Context) {
    val time = System.currentTimeMillis()
    content = "这是一个测试 <font color ='#38ADFF'><b>测试消息</b></font>"
    TaskManager.getInstance().addTask(object : BaseTask() {
        override fun doTask() {
            process++
            content += content
            RefreshNotifyManager.sendDownloadNotify(
                context,
                "kaaaa",
                "这是标题：$process",
                RefreshNotifyManager.getNotificationSubTitle(context, "这是副标题", time),
                content
            )
        }

        override fun getNextEarlyRunTime(): Int {
            return 0
        }

        override fun getMyInterval(): Int {
            return 2
        }

        override fun getTaskName(): String {
            return "SDDSD"
        }
    })
}

internal fun testNotify(context: Context) {
    ZixieContext.showToast("fsfd")
    NotifyManager.sendNotifyNow(
        context, "通知标题", "通知二级标题", "通知描述", "", "download"
    )
}

internal fun testNotifyProcess(context: Context) {
    DownloadNotifyManager.sendDownloadNotify(
        context,
        "https://blog.bihe0832.com/public/img/head.jpg",
        "王者荣耀",
        "https://blog.bihe0832.com/public/img/head.jpg",
        1000000,
        2345600789,
        239909 * process.toLong(),
        process,
        DownloadNotifyManager.DOWNLOAD_TYPE_DOWNLOADING,
        "download"
    )

    TaskManager.getInstance().addTask(object : BaseTask() {
        override fun doTask() {
            DownloadNotifyManager.sendDownloadNotify(
                context,
                "https://blog.bihe0832.com/public/img/head.jpg",
                "王者荣耀",
                "https://blog.bihe0832.com/public/img/head.jpg",
                1000000,
                2345600789,
                239909 * process.toLong(),
                process,
                DownloadNotifyManager.DOWNLOAD_TYPE_DOWNLOADING,
                "download"
            )
            process++
        }

        override fun getNextEarlyRunTime(): Int {
            return 0
        }

        override fun getMyInterval(): Int {
            return 2
        }

        override fun getTaskName(): String {
            return LOG_TAG
        }
    })
}