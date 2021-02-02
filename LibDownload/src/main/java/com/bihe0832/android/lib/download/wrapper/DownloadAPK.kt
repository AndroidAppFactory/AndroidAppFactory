package com.bihe0832.android.lib.download.wrapper

import android.app.Activity
import android.text.TextUtils
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.install.InstallUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.CommonDialog
import com.bihe0832.android.lib.ui.dialog.OnDialogListener


object DownloadAPK {

    private class InstallListener(
            private val activity: Activity,
            private val packageName: String,
            private val contentTitle: String,
            private val contentDesc: String,
            private val listener: OnDialogListener?) : SimpleDownloadListener() {
        override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
        }

        override fun onComplete(filePath: String, item: DownloadItem) {
            ZLog.i("startDownloadApk download installApkPath: $filePath")
            if (!TextUtils.isEmpty(contentTitle) && !TextUtils.isEmpty(contentDesc)) {
                ThreadManager.getInstance().runOnUIThread{
                    CommonDialog(activity).apply {
                        title = contentTitle
                        content = contentDesc
                        positive = "点击安装"
                        negative = "稍候安装"
                        setOnClickBottomListener(object : OnDialogListener {
                            override fun onPositiveClick() {
                                InstallUtils.installAPP(activity, filePath, packageName)
                                listener?.onPositiveClick()
                                dismiss()
                            }

                            override fun onNegativeClick() {
                                dismiss()
                                listener?.onNegativeClick()
                            }

                            override fun onCancel() {
                                listener?.onCancel()
                            }
                        })
                    }.let { it.show() }
                }
            }
            InstallUtils.installAPP(activity, filePath, packageName)
        }

        override fun onProgress(item: DownloadItem) {

        }
    }

    //直接下载，不显示进度，4G下载弹框，下载完成自动安装
    fun startDownload(activity: Activity, url: String, md5: String, packageName: String) {
        DownloadFile.startDownloadWithCheckAndProcess(
                activity,
                "", "",
                url, md5,
                true, false,
                null,
                InstallListener(activity, packageName, "", "", null))
    }

    //直接下载，显示进度，可以取消，4G下载弹框，下载完成自动安装
    fun startDownload(activity: Activity, title: String, msg: String, url: String, md5: String, packageName: String) {
        DownloadFile.startDownloadWithCheckAndProcess(
                activity,
                title, msg,
                url, md5,
                true, true,
                null,
                InstallListener(activity, packageName, title, msg, null))
    }

    //直接下载，显示进度，4G下载看参数，下载完成自动安装
    fun startDownload(activity: Activity, title: String, msg: String, url: String, md5: String, packageName: String, canCancel: Boolean, downloadMobile: Boolean, listener: OnDialogListener?) {
        DownloadFile.startDownloadWithProcess(
                activity,
                title, msg,
                url, md5,
                canCancel, downloadMobile,
                listener,
                InstallListener(activity, packageName, title, msg, listener))
    }

    //直接下载，不显示进度，4G下载不弹框直接下载，下载完成自动安装
    fun startDownloadWithoutCheck(activity: Activity, url: String, md5: String, packageName: String) {
        DownloadFile.startDownload(
                activity,
                "", "",
                url, md5,
                true, true,
                InstallListener(activity, packageName, "", "", null))
    }
}