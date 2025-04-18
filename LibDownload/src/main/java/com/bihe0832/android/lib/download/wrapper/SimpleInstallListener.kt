package com.bihe0832.android.lib.download.wrapper

import android.app.Activity
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.R
import com.bihe0832.android.lib.install.InstallListener
import com.bihe0832.android.lib.install.InstallUtils
import com.bihe0832.android.lib.install.InstallUtils.ApkInstallType
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.impl.LoadingDialog
import com.bihe0832.android.lib.ui.toast.ToastUtil

open class SimpleInstallListener(
    private val activity: Activity,
    private val packageName: String,
    private val listener: OnDialogListener?,
) : SimpleDownloadListener() {

    private val loadingDialog = LoadingDialog(activity).apply {
        setIsFullScreen(true)
        setCanCanceled(true)
        setOnCancelListener {
            ToastUtil.showShort(context, activity.getString(R.string.install_background))
        }
    }
    private val installListener = object : InstallListener {

        override fun onUnCompress() {
            loadingDialog.show(activity.getString(R.string.install_uncompress))
        }

        override fun onInstallPrepare() {
            loadingDialog.show(activity.getString(R.string.install_prepare))
        }

        override fun onInstallStart() {
            loadingDialog.dismiss()
        }

        override fun onInstallFailed(errorCode: Int) {
            loadingDialog.dismiss()
        }

        override fun onInstallSuccess() {
            loadingDialog.show(activity.getString(R.string.install_success))
        }

        override fun onInstallTimeOut() {
            loadingDialog.show(activity.getString(R.string.install_timeout))
        }
    }

    override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
        listener?.onNegativeClick()
    }

    override fun onComplete(filePath: String, item: DownloadItem): String {
        ZLog.i("startDownloadApk download installApkPath: $filePath")
        ThreadManager.getInstance().runOnUIThread {
            if (InstallUtils.getFileType(filePath) == ApkInstallType.APK) {
                InstallUtils.installAPP(activity, filePath, packageName, installListener)
            }
        }
        return filePath
    }

    override fun onProgress(item: DownloadItem) {
    }
}
