package com.bihe0832.android.common.message.base

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import com.bihe0832.android.common.message.MessageListLiveData
import com.bihe0832.android.common.message.data.MessageInfoItem
import com.bihe0832.android.common.message.data.db.MessageDBManager
import com.bihe0832.android.framework.router.RouterAction
import com.bihe0832.android.framework.router.openZixieWeb
import com.bihe0832.android.lib.download.wrapper.DownloadAPK
import com.bihe0832.android.lib.http.common.HTTPServer
import com.bihe0832.android.lib.http.common.HttpResponseHandler
import com.bihe0832.android.lib.http.common.core.HttpBasicRequest
import com.bihe0832.android.lib.lifecycle.ApplicationObserver
import com.bihe0832.android.lib.lifecycle.LifecycleHelper
import com.bihe0832.android.lib.ui.dialog.CommonDialog
import com.bihe0832.android.lib.ui.dialog.OnDialogListener
import java.net.HttpURLConnection

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2023/2/28.
 * Description: Description
 *
 */
abstract class MessageManager {
    // AAF 框架的公共通知
    private val AAF_COMMON_MESSAGE = "https://cdn.bihe0832.com/app/msg/aaf_msg.json"

    abstract fun fetchNewMsg()

    fun getMessageLiveData(): MessageListLiveData {
        return MessageListLiveData
    }

    open fun initModule(context: Context) {
        MessageListLiveData.initData(context)
        ApplicationObserver.addStatusChangeListener(object : ApplicationObserver.APPStatusChangeListener {
            override fun onBackground() {

            }

            override fun onForeground() {
                if (LifecycleHelper.getCurrentTime() - ApplicationObserver.getLastPauseTime() > 5 * 60 * 1000) {
                    updateMsg()
                }
            }
        })
        updateMsg()
    }

    fun updateMsg() {
        fetchMessageByFile(AAF_COMMON_MESSAGE)
        fetchNewMsg()
    }


    fun fetchMessageByFile(url: String) {
        HTTPServer.getInstance().doRequestAsync(object : HttpBasicRequest() {
            override fun getUrl(): String {
                return url
            }

            override fun getResponseHandler(): HttpResponseHandler {
                return HttpResponseHandler { statusCode, msg ->
                    if (HttpURLConnection.HTTP_OK == statusCode && !TextUtils.isEmpty(msg)) {
                        MessageListLiveData.parseMessage(msg)
                    }
                }
            }
        })
    }

    fun showMessage(activity: Activity, item: MessageInfoItem, showFace: Boolean, listener: OnDialogListener?) {
        when (item.type) {
            MessageInfoItem.TYPE_TEXT, MessageInfoItem.TYPE_IMG, MessageInfoItem.TYPE_APK -> {
                CommonDialog(activity).apply {
                    setTitle(item.title)
                    setSingle(TextUtils.isEmpty(item.action))
                    setPositive(if (TextUtils.isEmpty(item.action)) {
                        "确定"
                    } else {
                        if (item.type == MessageInfoItem.TYPE_APK) {
                            "立刻下载"
                        } else {
                            "前往"
                        }
                    })
                    setNegative("关闭")
                    setOnClickBottomListener(object : OnDialogListener {
                        override fun onPositiveClick() {
                            if (!TextUtils.isEmpty(item.action)) {
                                if (item.type == MessageInfoItem.TYPE_APK) {
                                    DownloadAPK.startDownloadWithCheck(activity, item.action, "", "")
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
            }
        }
        MessageListLiveData.updateMessageFlag(item.messageID, hasRead = true, isDel = false)
        if (showFace && item.showFace > 0) {
            MessageListLiveData.updateMessageFace(item.messageID, item.showFace - 1)
        }
    }

    fun deleteMessage(messageInfoItem: MessageInfoItem?) {
        messageInfoItem?.let {
            MessageDBManager.deleteData(messageInfoItem)
        }
    }

}