package com.bihe0832.android.lib.download.wrapper

import android.app.Activity
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.install.InstallListener
import com.bihe0832.android.lib.install.InstallUtils
import com.bihe0832.android.lib.install.InstallUtils.ApkInstallType
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.LoadingDialog
import com.bihe0832.android.lib.ui.dialog.OnDialogListener
import com.bihe0832.android.lib.ui.toast.ToastUtil

open class SimpleInstallListener(
        private val activity: Activity,
        private val packageName: String,
        private val listener: OnDialogListener?) : SimpleDownloadListener() {

    private val loadingDialog = LoadingDialog(activity).apply {
        setIsFullScreen(true)
        setCanCanceled(true)
        setOnCancelListener {
            ToastUtil.showShort(context, "应用安装已切到后台，请耐心等待")
        }
    }
    private val installListener = object : InstallListener {

        override fun onUnCompress() {
            loadingDialog.show("特殊应用安装较慢，请耐心等待 <BR><BR> 当前安装包解压中，请稍候...")
        }

        override fun onInstallPrepare() {
            loadingDialog.show("特殊应用安装较慢，请耐心等待 <BR><BR> 当前安装包完整性检查，请稍候...")
        }

        override fun onInstallStart() {
            loadingDialog.dismiss()
        }

        override fun onInstallFailed(errorCode: Int) {
            loadingDialog.dismiss()
        }
    }

    override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
        listener?.onNegativeClick()
    }

    override fun onComplete(filePath: String, item: DownloadItem) {
        ZLog.i("startDownloadApk download installApkPath: $filePath")
        ThreadManager.getInstance().runOnUIThread {
            if (InstallUtils.getFileType(filePath) == ApkInstallType.APK) {
                InstallUtils.installAPP(activity, filePath, packageName)
            } else {
                InstallUtils.installAPP(activity, filePath, packageName, installListener)
            }
        }
    }

    override fun onProgress(item: DownloadItem) {

    }
}