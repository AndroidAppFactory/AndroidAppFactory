package com.bihe0832.android.common.message

import android.content.Context
import androidx.lifecycle.MediatorLiveData
import com.bihe0832.android.common.message.data.MessageInfoItem
import com.bihe0832.android.common.message.data.db.MessageDBManager
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.lifecycle.LifecycleHelper
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.notification.NotifyManager
import com.bihe0832.android.lib.thread.ThreadManager
import java.sql.SQLException


/**
 * @author zixie code@bihe0832.com
 * Created on 2019-09-17.
 * Description: Description
 */

object MessageListLiveData : MediatorLiveData<List<MessageInfoItem>>() {

    val TAG = "Message-> "
    private const val lockdata = false

    fun initData(context: Context) {
        MessageDBManager.init(context)
        sortMessage(MessageDBManager.getAll()).let { list ->
            ThreadManager.getInstance().runOnUIThread {
                postValue(list.filter { it.isNotExpired && !it.hasDelete() })
                ZLog.d(TAG, "updateData value length:" + value?.size)
            }
            list.forEach { messageInfo ->
                if (!messageInfo.isNotExpired) {
                    ThreadManager.getInstance().run {
                        MessageDBManager.deleteData(messageInfo)
                    }
                }
            }
        }
    }

    fun parseMessage(httpResultList: List<MessageInfoItem>) {
        if (httpResultList == null || httpResultList.isEmpty()) {
            ZLog.d(TAG, "httpResultList = null or size is 0")
        } else {
            synchronized(lockdata) {
                //本地已经有的列表
                var msgListInDB = HashMap<String, MessageInfoItem>().apply {
                    MessageDBManager.getAll().let { messgeInfo ->
                        messgeInfo.forEach {
                            put(it.messageID, it)
                        }
                    }
                }

                // 保存数据库，并将已经读取的数据结合网络数据更新到最新
                var ignoreList = ArrayList<String>()
                for (index in httpResultList.indices) {
                    var infoFromServer = httpResultList[index]
                    if (msgListInDB.keys.contains(infoFromServer.messageID) && null != msgListInDB[infoFromServer.messageID]) {
                        msgListInDB[infoFromServer.messageID]!!.let { messageInfoDB ->
                            infoFromServer.apply {
                                this.messageID = messageInfoDB.messageID
                                this.isNotify = messageInfoDB.isNotify
                                this.setHasDelete(messageInfoDB.hasDelete())
                                this.setHasRead(messageInfoDB.hasRead())
                                this.showFace = messageInfoDB.showFace
                                this.lastShow = messageInfoDB.lastShow
                            }.let {
                                ZLog.d(TAG, "本地已有再次下发：本地数据：$messageInfoDB ,下发数据: $it")
                                messageInfoDB.copyFrom(it)
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
                        value = sortMessage(finalResult).toMutableList()
                    }
                }
        }
    }

    @Synchronized
    private fun sortMessage(finalResult: List<MessageInfoItem>): List<MessageInfoItem> {
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
    fun updateMessageLocalStatus(msgid: String, hasRead: Boolean, showFace: Int, isDel: Boolean) {
        ThreadManager.getInstance().start {
            synchronized(lockdata) {
                try {
                    value?.let { list ->
                        list.find { it.messageID == msgid }?.let {
                            it.setHasRead(hasRead)
                            it.setHasDelete(isDel)
                            it.showFace = showFace
                            it.lastShow = LifecycleHelper.getCurrentTime()
                            MessageDBManager.saveData(it)
                        }
                        if (isDel) {
                            ThreadManager.getInstance().runOnUIThread {
                                value = list.filter { it.messageID != msgid }.toMutableList()
                            }
                        }
                        postValue(list.toList())
                    }

                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
        }
    }
}