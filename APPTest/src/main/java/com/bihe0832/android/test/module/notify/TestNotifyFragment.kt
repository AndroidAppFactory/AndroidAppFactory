package com.bihe0832.android.test.module.notify

import android.os.Bundle
import com.bihe0832.android.lib.http.common.HttpBasicRequest.LOG_TAG
import com.bihe0832.android.lib.notification.DownloadNotifyManager
import com.bihe0832.android.lib.notification.NotifyManager
import com.bihe0832.android.lib.router.Routers
import com.bihe0832.android.lib.timer.BaseTask
import com.bihe0832.android.lib.timer.TaskManager
import com.bihe0832.android.test.base.BaseTestFragment
import com.bihe0832.android.test.base.OnTestItemClickListener
import com.bihe0832.android.test.base.TestItem
import com.bihe0832.android.test.module.TestDebugCommonFragment
import com.bihe0832.android.test.module.touch.TouchRegionActivity
import java.util.*

class TestNotifyFragment : BaseTestFragment() {
    var process = 0
    override fun getDataList(): List<TestItem> {
        val items: MutableList<TestItem> = ArrayList()
        items.add(TestItem("简单测试函数") { testNotify() })
        items.add(TestItem("通用测试预处理") { testNotifyProcess() })
        return items
    }

    companion object {
        fun newInstance(): TestNotifyFragment {
            val args = Bundle()
            val fragment = TestNotifyFragment()
            fragment.arguments = args
            return fragment
        }
    }

    fun testNotify() {
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