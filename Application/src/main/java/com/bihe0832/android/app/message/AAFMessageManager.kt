package com.bihe0832.android.app.message

import android.app.Activity
import com.bihe0832.android.app.R
import com.bihe0832.android.app.api.AAFNetWorkApi
import com.bihe0832.android.common.message.base.MessageManager
import com.bihe0832.android.common.message.data.MessageInfoItem
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.blockdialog.DependenceBlockDialogManager

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2019-10-21.
 * Description: Description
 *
 */
object AAFMessageManager : MessageManager() {

    private val mDependenceBlockDialogManager by lazy {
        DependenceBlockDialogManager(true)
    }

    override fun fetchNewMsg() {
        fetchMessageByFile(AAFNetWorkApi.getCommonURL(ZixieContext.applicationContext?.getString(R.string.message_url)
                ?: "", ""))
    }

    fun showMessage(activity: Activity, messageInfoItem: MessageInfoItem, showFace: Boolean) {
        mDependenceBlockDialogManager.getDependentTaskManager().addTask(messageInfoItem.messageID, {
            ThreadManager.getInstance().runOnUIThread {
                showMessage(activity, messageInfoItem, showFace, object : OnDialogListener {
                    override fun onPositiveClick() {
                        mDependenceBlockDialogManager.getDependentTaskManager().finishTask(messageInfoItem.messageID)
                    }

                    override fun onNegativeClick() {
                        onPositiveClick()
                    }

                    override fun onCancel() {
                        onPositiveClick()
                    }
                })
            }
        }, mutableListOf())
    }
}