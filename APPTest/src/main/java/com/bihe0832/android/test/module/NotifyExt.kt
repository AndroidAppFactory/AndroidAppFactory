package com.bihe0832.android.test.module

import com.bihe0832.android.lib.notification.NotifyManager
import com.bihe0832.android.lib.timer.BaseTask
import com.bihe0832.android.lib.timer.TaskManager
import com.bihe0832.android.test.MainActivity

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2020/6/15.
 * Description: Description
 *
 */

fun MainActivity.testNotify() {
    NotifyManager.sendNotifyNow(applicationContext,
            "通知标题",
            "通知二级标题",
            "通知描述", "", "download")
}

fun MainActivity.testNotifyProcess(noticeId: Int) {
    TaskManager.getInstance().addTask(object : BaseTask() {
        override fun run() {
            NotifyManager.sendDownloadNotify(applicationContext,
                    "王者荣耀",
                    1000000, 2345600789, 239909 * process.toLong(), process, NotifyManager.DOWNLOAD_TYPE_DOWNLOADING, "download", noticeId)
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