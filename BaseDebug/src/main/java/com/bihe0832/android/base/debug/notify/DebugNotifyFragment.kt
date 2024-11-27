package com.bihe0832.android.base.debug.notify

import android.view.View
import com.bihe0832.android.app.api.AAFNetWorkApi.LOG_TAG
import com.bihe0832.android.common.permission.AAFPermissionManager
import com.bihe0832.android.common.debug.base.BaseDebugListFragment
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.notification.DownloadNotifyManager
import com.bihe0832.android.lib.notification.NotifyManager
import com.bihe0832.android.lib.notification.RefreshNotifyManager
import com.bihe0832.android.lib.timer.BaseTask
import com.bihe0832.android.lib.timer.TaskManager

class DebugNotifyFragment : BaseDebugListFragment() {

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getDebugItem("是否开启通知", View.OnClickListener { AAFPermissionManager.hasNotifyPermission() }))
            add(getDebugItem("打开通知设置", View.OnClickListener { AAFPermissionManager.openNotifyPermission() }))
            add(getDebugItem("普通通知", View.OnClickListener { testNotify() }))
            add(getDebugItem("可刷新通知", View.OnClickListener { testRefreshNotify() }))
            add(getDebugItem("下载通知", View.OnClickListener { testNotifyProcess() }))

        }
    }

    var process = 0
    var content = "这是一个测试"
    fun testRefreshNotify() {

        var time = System.currentTimeMillis()
        content = "这是一个测试 <font color ='#38ADFF'><b>测试消息</b></font>"
        TaskManager.getInstance().addTask(object : BaseTask() {
            override fun run() {
                process++
                content += content
                RefreshNotifyManager.sendDownloadNotify(context!!,
                        "kaaaa", "这是标题：$process", RefreshNotifyManager.getNotificationSubTitle(context!!, "这是副标题", time), content)
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

    private fun testNotify() {
        ZixieContext.showToast("fsfd")
        NotifyManager.sendNotifyNow(context!!,
                "通知标题",
                "通知二级标题",
                "通知描述", "", "download")
    }

    fun testNotifyProcess() {
        DownloadNotifyManager.sendDownloadNotify(context!!,
                "https://blog.bihe0832.com/public/img/head.jpg", "王者荣耀",
                "https://blog.bihe0832.com/public/img/head.jpg", 1000000, 2345600789, 239909 * process.toLong(), process, DownloadNotifyManager.DOWNLOAD_TYPE_DOWNLOADING, "download")

        TaskManager.getInstance().addTask(object : BaseTask() {
            override fun run() {
                DownloadNotifyManager.sendDownloadNotify(context!!!!,
                        "https://blog.bihe0832.com/public/img/head.jpg", "王者荣耀",
                        "https://blog.bihe0832.com/public/img/head.jpg", 1000000, 2345600789, 239909 * process.toLong(), process, DownloadNotifyManager.DOWNLOAD_TYPE_DOWNLOADING, "download")
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
}