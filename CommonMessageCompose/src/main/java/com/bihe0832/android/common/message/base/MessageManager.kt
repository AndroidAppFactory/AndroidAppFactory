package com.bihe0832.android.common.message.base

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import com.bihe0832.android.common.message.MessageListLiveData
import com.bihe0832.android.common.message.R
import com.bihe0832.android.common.message.data.MessageInfoItem
import com.bihe0832.android.common.message.data.db.MessageDBManager
import com.bihe0832.android.framework.router.RouterAction
import com.bihe0832.android.framework.router.openZixieWeb
import com.bihe0832.android.lib.download.wrapper.DownloadAPK
import com.bihe0832.android.lib.gson.JsonHelper
import com.bihe0832.android.lib.http.common.HTTPServer
import com.bihe0832.android.lib.lifecycle.ApplicationObserver
import com.bihe0832.android.lib.lifecycle.LifecycleHelper
import com.bihe0832.android.lib.lifecycle.LifecycleHelper.getAPPCurrentStartTime
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.CommonDialog
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.model.res.R as ModelResR
import java.net.HttpURLConnection

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2023/2/28.
 * Description: Description
 *
 */
public open class MessageManager {

    open fun fetchNewMsg() {
        fetchMessageByURLList(ThemeResourcesManager.getString(R.string.com_bihe0832_message_common_msg_url)!!)
    }

    fun getMessageLiveData(): MessageListLiveData {
        return MessageListLiveData
    }

    open fun initModule(context: Context) {
        ZLog.d(MessageListLiveData.TAG, "MessageManager initModule ")
        MessageListLiveData.initData(context)
        ApplicationObserver.addStatusChangeListener(object :
            ApplicationObserver.APPStatusChangeListener {
            override fun onBackground() {
            }

            override fun onForeground() {
                if (ApplicationObserver.getLastPauseTime() > 0 && LifecycleHelper.getCurrentTime() - ApplicationObserver.getLastPauseTime() > 5 * 60 * 1000) {
                    updateMsg()
                }
            }
        })
        updateMsg()
    }

    fun updateMsg() {
        ZLog.d(MessageListLiveData.TAG, "MessageManager updateMsg ")
        fetchNewMsg()
    }

    fun fetchMessageByURLList(url: String) {
        url.split(";").forEach {
            fetchMessageByURL(it)
        }
    }

    private fun fetchMessageByURL(url: String) {
        if (URLUtils.isHTTPUrl(url)) {
            HTTPServer.getInstance().doRequest(url) { statusCode, msg ->
                if (HttpURLConnection.HTTP_OK == statusCode && !TextUtils.isEmpty(msg)) {
                    ThreadManager.getInstance().start {
                        ZLog.d(MessageListLiveData.TAG, "fetchMessageByFile url:$url")
                        ZLog.d(MessageListLiveData.TAG, "fetchMessageByFile result:$msg")
                        val httpResultList: ArrayList<MessageInfoItem> = ArrayList()
                        try {
                            JsonHelper.fromJsonList(msg, MessageInfoItem::class.java)
                                ?.filter { it.isNotExpired }?.let { msgJsonResponse ->
                                    httpResultList.addAll(msgJsonResponse)
                                }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        ZLog.d(
                            MessageListLiveData.TAG,
                            "fetchMessageByFile parse result:" + httpResultList.size,
                        )
                        MessageListLiveData.parseMessage(httpResultList)
                    }
                }
            }
        }
    }

    fun showMessage(
        activity: Activity,
        item: MessageInfoItem,
        showFace: Boolean,
        listener: OnDialogListener?
    ) {
        when (item.type) {
            MessageInfoItem.TYPE_TEXT, MessageInfoItem.TYPE_IMG, MessageInfoItem.TYPE_APK -> {
                CommonDialog(activity).apply {
                    setTitle(item.title)
                    setSingle(TextUtils.isEmpty(item.action))
                    setPositive(
                        if (TextUtils.isEmpty(item.action)) {
                            activity.getString(ModelResR.string.com_bihe0832_message_confirm)
                        } else {
                            if (item.type == MessageInfoItem.TYPE_APK) {
                                activity.getString(ModelResR.string.com_bihe0832_message_download)
                            } else {
                                activity.getString(ModelResR.string.com_bihe0832_message_go)
                            }
                        },
                    )
                    setNegative(activity.getString(ModelResR.string.com_bihe0832_message_close))
                    setOnClickBottomListener(object : OnDialogListener {
                        override fun onPositiveClick() {
                            if (!TextUtils.isEmpty(item.action)) {
                                if (item.type == MessageInfoItem.TYPE_APK) {
                                    DownloadAPK.startDownloadWithCheck(
                                        activity,
                                        item.action,
                                        "",
                                    )
                                } else {
                                    RouterAction.openFinalURL(item.action)
                                }
                            }
                            listener?.onPositiveClick()
                            dismiss()
                        }

                        override fun onNegativeClick() {
                            listener?.onNegativeClick()
                            dismiss()
                        }

                        override fun onCancel() {
                            listener?.onCancel()
                        }
                    })
                    setShouldCanceled(TextUtils.isEmpty(item.action))
                    if (item.type == MessageInfoItem.TYPE_IMG) {
                        setImageUrl(item.content)
                    } else {
                        setHtmlContent(item.content)
                    }
                }.show()
            }

            MessageInfoItem.TYPE_WEB_PAGE -> {
                openZixieWeb(item.content)
                listener?.onPositiveClick()
            }
        }
        val face = if (showFace && item.showFace > 0) {
            item.showFace - 1
        } else {
            item.showFace
        }
        MessageListLiveData.updateMessageLocalStatus(
            item.messageID,
            hasRead = true,
            showFace = face,
            isDel = false
        )
    }

    fun deleteMessage(messageInfoItem: MessageInfoItem?) {
        messageInfoItem?.let {
            MessageDBManager.deleteData(messageInfoItem)
        }
    }

    /**
     * @param showAgain 在本次启动中，如果已经拍过脸，再次拉取到是否要二次拍脸
     * @return
     */
    open fun canShowFace(messageInfoItem: MessageInfoItem, showAgain: Boolean): Boolean {
        return if (messageInfoItem.showFace > 0 || messageInfoItem.showFace == -1) {
            val canShow = messageInfoItem.isNotExpired && !messageInfoItem.hasDelete()
            if (showAgain) {
                canShow
            } else {
                val notShowThisStart =
                    MessageDBManager.getData(messageInfoItem.messageID)?.lastShow ?: messageInfoItem.lastShow < getAPPCurrentStartTime()
                canShow && notShowThisStart
            }
        } else {
            false
        }
    }

    fun getUnreadNum(): Int {
        getMessageLiveData().value?.filter { !it.hasRead() }?.let {
            if (it.isNotEmpty()) {
                return it.size
            }
        }
        return 0
    }
}
