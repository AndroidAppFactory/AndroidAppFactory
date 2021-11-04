package com.bihe0832.android.base.test.notify

import android.view.View
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.http.common.HttpBasicRequest.LOG_TAG
import com.bihe0832.android.lib.notification.DownloadNotifyManager
import com.bihe0832.android.lib.notification.NotifyManager
import com.bihe0832.android.lib.timer.BaseTask
import com.bihe0832.android.lib.timer.TaskManager
import com.bihe0832.android.common.test.base.BaseTestListFragment
import com.bihe0832.android.common.test.item.TestItemData

class TestNotifyFragment : BaseTestListFragment() {

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(TestItemData("简单测试函数", View.OnClickListener { testNotify() }))
            add(TestItemData("通用测试预处理", View.OnClickListener { testNotifyProcess() }))
        }
    }

    var process = 0

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