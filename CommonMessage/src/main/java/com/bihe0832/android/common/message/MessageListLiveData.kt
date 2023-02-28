package com.bihe0832.android.common.message

import android.content.Context
import androidx.lifecycle.MediatorLiveData
import com.bihe0832.android.common.message.data.MessageInfoItem
import com.bihe0832.android.common.message.data.db.MessageDBManager
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.gson.JsonHelper
import com.bihe0832.android.lib.lifecycle.LifecycleHelper
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.notification.NotifyManager
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.ConvertUtils
import java.sql.SQLException
import java.util.*


/**
 * @author hardyshi code@bihe0832.com
 * Created on 2019-09-17.
 * Description: Description
 */

object MessageListLiveData : MediatorLiveData<List<MessageInfoItem>>() {

    private val TAG = "MessageListLiveData-> "

    fun initData(context: Context) {
        MessageDBManager.init(context)
        sortNotice(MessageDBManager.getAll()).let { list ->
            ThreadManager.getInstance().runOnUIThread {
                postValue(list.filter { it.isNotExpired && !it.hasDelete() })
                ZLog.d(TAG, "updateData value length:" + value?.size)
            }
            list.forEach { noticeInfo ->
                if (!noticeInfo.isNotExpired) {
                    ThreadManager.getInstance().run {
                        MessageDBManager.deleteData(noticeInfo)
                    }
                }
            }
        }
    }


    @Synchronized
    fun parseNotice(resultJson: String) {
        ZLog.d(TAG, "parseNotice:$resultJson")
        var httpResultList: List<MessageInfoItem> = ArrayList()
        try {
            JsonHelper.fromJsonList(resultJson, MessageInfoItem::class.java)?.filter { it.isNotExpired }?.let { msgJsonResponse ->
                httpResultList = msgJsonResponse
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (httpResultList == null || httpResultList.isEmpty()) {
            ZLog.d(TAG, "httpResultList = null or size is 0")
            return
        }

        //本地已经有的列表
        var msgListInDB = HashMap<String, MessageInfoItem>().apply {
            MessageDBManager.getAll().let { noticeInfo ->
                noticeInfo.forEach {
                    put(it.messageID, it)
                }
            }
        }

        // 保存数据库，并将已经读取的数据结合网络数据更新到最新
        var ignoreList = ArrayList<String>()
        for (index in httpResultList.indices) {
            var infoFromServer = httpResultList[index]
            if (msgListInDB.keys.contains(infoFromServer.messageID) && null != msgListInDB[infoFromServer.messageID]) {
                msgListInDB[infoFromServer.messageID]!!.let { noticeInfoDB ->
                    infoFromServer.apply {
                        this.messageID = noticeInfoDB.messageID
                        this.isNotify = noticeInfoDB.isNotify
                        this.setHasDelete(noticeInfoDB.hasDelete())
                        this.setHasRead(noticeInfoDB.hasRead())
                        this.showFace = noticeInfoDB.showFace
                    }.let {
                        ZLog.d(TAG, "本地已有再次下发：本地数据：$noticeInfoDB ,下发数据: $it")
                        noticeInfoDB.copyFrom(it)
                    }
                }
                MessageDBManager.saveData(infoFromServer)
                ignoreList.add(infoFromServer.messageID)
            } else {
                MessageDBManager.saveData(infoFromServer)
            }
        }

        //剔除网络请求重复的公告
        var newHttpResultList = httpResultList.toMutableList().filter { !ignoreList.contains(it.messageID) }

        //通知栏通知
        sendNotify(newHttpResultList)
        ZLog.d(TAG, "value length:" + value?.size)

        var finalResult = (newHttpResultList + msgListInDB.values).filter { it.isNotExpired && !it.hasDelete() }
        ThreadManager.getInstance().runOnUIThread {
            value = sortNotice(finalResult).toMutableList()
        }
    }

    @Synchronized
    private fun sortNotice(finalResult: List<MessageInfoItem>): List<MessageInfoItem> {
        return finalResult.sortedWith(compareBy { it.messageID }).sortedWith(compareBy { it.shouldTop }).reversed()
    }

    private fun sendNotify(list: List<MessageInfoItem>?) {
        ZixieContext.applicationContext?.let { context ->
            list?.let {
                for (mNotifyInfoItem in list) {
                    if (mNotifyInfoItem.isNotify == "1") {
                        mNotifyInfoItem.apply { isNotify = "0" }.let {
                            NotifyManager.createNotificationChannel(context, it.notifyChannelName, it.notifyChannelID)
                            NotifyManager.sendNotifyNow(context, it.title, "", it.notifyDesc, it.action, it.notifyChannelID)
                            MessageDBManager.saveData(it)
                        }
                    }
                }
            }
        }
    }

    //更新 是否已读，flag;更新是否删除，目前仅做删除标记 ，isDel
    fun updateNoticeFlag(msgid: String, hasRead: Boolean, isDel: Boolean) {
        try {
            value?.let { list ->
                list.find { it.messageID == msgid }?.let {
                    it.setHasRead(hasRead)
                    it.setHasDelete(isDel)
                    it.lastShow = LifecycleHelper.getCurrentTime()
                    ThreadManager.getInstance().run {
                        MessageDBManager.saveData(it)
                    }
                }
                if (isDel) {
                    ThreadManager.getInstance().runOnUIThread {
                        value = list.filter { it.messageID != msgid }.toMutableList()
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    fun updateNoticeFace(msgid: String, showFace: Int) {
        try {
            value?.let { list ->
                list.find { it.messageID == msgid }?.let {
                    it.showFace = showFace
                    ThreadManager.getInstance().run {
                        MessageDBManager.saveData(it)
                    }
                }
            }

        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }
}