package com.bihe0832.android.test.module

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.timer.BaseTask
import com.bihe0832.android.lib.timer.TaskManager
import com.bihe0832.android.lib.ui.dialog.*
import com.bihe0832.android.lib.ui.toast.ToastUtil
import com.bihe0832.android.lib.utils.intent.IntentUtils
import com.bihe0832.android.test.MainActivity
import com.bihe0832.android.test.R

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2020/6/15.
 * Description: Description
 *
 */

fun MainActivity.testAlert(activity: Activity) {
    var title = "分享的标题"
    var content = "分享的内容"
    val dialog = CommonDialog(activity)
    dialog.setTitle(title)
    dialog.setPositive("分享给我们")
    dialog.setContent("调试信息已经准备好，你可以直接「分享给我们」或将信息「复制到剪贴板」后转发给我们")
    dialog.setImageContentResId(R.mipmap.debug)
    dialog.setFeedBackContent("我们保证你提供的信息仅用于问题定位")
    dialog.setNegtive("复制到剪切板")
    dialog.setOnClickBottomListener(object : OnDialogListener {
        override fun onPositiveClick() {
            try {
                IntentUtils.sendTextInfo(activity, title, content)
                dialog.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onNegtiveClick() {
            try {
                val cm = activity.getSystemService(Context.CLIPBOARD_SERVICE) as (ClipboardManager)
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
                    cm.primaryClip = ClipData.newPlainText(null, content)
                } else {
                    cm.text = content
                }
                dialog.dismiss()
                ToastUtil.showShort(activity, "信息已保存到剪贴板")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onCloseClick() {}
    })
    dialog.show()
}


fun MainActivity.testUpdate(activity: Activity) {
    var taskName = "TEST"
    var progressDialog = CommonDialogNew(activity).apply {
        setTitle("更新测试")
        setMessage("正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~")
        setCurrentSize(1)
        setAPKSize(10000)
        setNegtive("取消下载")
        setPositive("后台下载")
        setShouldCanceled(false)
        setOnClickListener(object : OnDialogListener{
            override fun onPositiveClick() {
                dismiss()
                TaskManager.getInstance().removeTask(taskName)
            }

            override fun onNegtiveClick() {
                dismiss()
                TaskManager.getInstance().removeTask(taskName)
            }

            override fun onCloseClick() {
                dismiss()
                TaskManager.getInstance().removeTask(taskName)
            }
        })
    }
    ThreadManager.getInstance().runOnUIThread { progressDialog.show() }
    var i = 0

    TaskManager.getInstance().addTask(object : BaseTask(){
        override fun run() {
            if(i< 100 ){
                activity.runOnUiThread(Runnable {
                    progressDialog.setAPKSize(10000)
                    progressDialog.setCurrentSize(100 * i)
                })
            }else{
                TaskManager.getInstance().removeTask(taskName)
            }
            i++
        }

        override fun getNextEarlyRunTime(): Int {
            return 0
        }

        override fun getMyInterval(): Int {
            return  1
        }

        override fun getTaskName(): String {
            return taskName
        }
    })
}


fun MainActivity.testLoading(activity: Activity) {
    LoadingDialog(activity).apply {
        setIsFullScreen(true)
        setCanCanceled(false)
    }.let {
        it.show("这是一个测试~")
    }
}
