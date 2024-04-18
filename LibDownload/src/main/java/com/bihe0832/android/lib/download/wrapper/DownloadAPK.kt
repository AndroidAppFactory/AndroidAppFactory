package com.bihe0832.android.lib.download.wrapper

import android.app.Activity
import android.content.Context
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadListener
import com.bihe0832.android.lib.install.InstallUtils
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener


object DownloadAPK {

    //直接下载，不显示进度，4G下载弹框，下载完成自动安装且弹框
    fun startDownloadWithCheckAndProcess(activity: Activity, url: String) {
        startDownloadWithCheck(activity, url, "", "")
    }


    //直接下载，显示进度，可以取消，4G下载弹框，下载完成自动安装且弹框
    fun startDownloadWithCheckAndProcess(
        activity: Activity,
        title: String,
        msg: String,
        url: String,
        md5: String,
        packageName: String
    ) {
        startDownloadWithCheckAndProcess(
            activity,
            title, msg,
            url, md5,
            packageName,
            true, null
        )
    }


    //直接下载，显示进度，4G下载弹框，顺序下载，下载完成自动安装且弹框
    fun startDownloadWithCheckAndProcess(
        activity: Activity,
        title: String,
        msg: String,
        url: String,
        md5: String,
        packageName: String,
        canCancel: Boolean,
        listener: OnDialogListener?
    ) {
        DownloadFile.downloadWithCheckAndProcess(
            activity,
            title, msg,
            url, "", false, md5, "",
            canCancel,
            true,
            forceDownload = false,
            needRecord = false,
            listener,
            SimpleInstallListener(activity, packageName, listener)
        )
    }

    //直接下载，显示进度，4G下载看参数，强制下载，下载完成自动安装且弹框
    fun startDownloadWithProcess(
        activity: Activity,
        title: String,
        msg: String,
        url: String,
        md5: String,
        packageName: String,
        canCancel: Boolean,
        downloadMobile: Boolean,
        listener: OnDialogListener?
    ) {
        startDownloadWithProcess(
            activity,
            title, msg,
            url, md5,
            canCancel, downloadMobile,
            listener,
            SimpleInstallListener(activity, packageName, listener)
        )
    }

    fun startDownloadWithProcess(
        activity: Activity,
        title: String,
        msg: String,
        url: String,
        md5: String,
        canCancel: Boolean,
        downloadMobile: Boolean,
        listener: OnDialogListener?,
        downloadListener: DownloadListener?
    ) {
        DownloadFile.downloadWithProcess(
            activity,
            title, msg,
            url, "", false, md5, "",
            canCancel, forceDownloadNew = false, downloadMobile, forceDownload = true, needRecord = false,
            listener,
            downloadListener
        )
    }

    //直接下载，显示进度，4G下载弹框，强制下载，下载完成自动安装且弹框
    fun startDownloadWithCheck(activity: Activity, url: String, md5: String, packageName: String) {
        DownloadFile.downloadWithCheckAndProcess(
            activity,
            "", "",
            url, "", false, md5, "",
            canCancel = true, useProcess = false,
            forceDownload = true, needRecord = false,
            listener = null,
            downloadListener = object : SimpleDownloadListener() {
                override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                }

                override fun onComplete(filePath: String, item: DownloadItem): String {
                    ThreadManager.getInstance().runOnUIThread {
                        InstallUtils.installAPP(activity, filePath, packageName)
                    }
                    return filePath
                }

                override fun onProgress(item: DownloadItem) {
                }

            })
    }

    private class SimpleAPKDownloadListener(
        private val context: Context,
        private val packageName: String
    ) : SimpleDownloadListener() {
        override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
        }

        override fun onComplete(filePath: String, item: DownloadItem): String {
            ThreadManager.getInstance().runOnUIThread {
                InstallUtils.installAPP(context, filePath, packageName)
            }
            return filePath
        }

        override fun onProgress(item: DownloadItem) {
        }

    }

    //直接下载，不显示进度，4G下载不弹框直接下载，下载完成自动安装
    fun forceDownload(context: Context, url: String, md5: String, packageName: String) {
        DownloadTools.startDownload(
            context,
            "",
            "",
            url,
            "",
            false,
            md5,
            sha256 = "",
            forceDownloadNew = true,
            useMobile = true,
            actionKey = DownloadUtils.DOWNLOAD_ACTION_KEY_APK,
            forceDownload = false,
            needRecord = false,
            downloadListener = SimpleAPKDownloadListener(context, packageName)
        )
    }

    fun download(context: Context, url: String, md5: String, packageName: String) {
        DownloadTools.startDownload(
            context,
            "",
            "",
            url,
            "",
            false,
            md5,
            sha256 = "",
            forceDownloadNew = false,
            useMobile = true,
            actionKey = DownloadUtils.DOWNLOAD_ACTION_KEY_APK,
            forceDownload = false,
            needRecord = false,
            downloadListener = SimpleAPKDownloadListener(context, packageName)
        )

    }
}