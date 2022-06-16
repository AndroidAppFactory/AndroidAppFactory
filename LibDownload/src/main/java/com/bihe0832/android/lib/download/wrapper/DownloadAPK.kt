package com.bihe0832.android.lib.download.wrapper

import android.app.Activity
import android.content.Context
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.install.InstallUtils
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.OnDialogListener


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


    //直接下载，显示进度，4G下载弹框，下载完成自动安装且弹框
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
        DownloadFile.startDownloadWithCheckAndProcess(
                activity,
                title, msg,
                url, md5,
                canCancel,
                listener,
                SimpleInstallListener(activity, packageName, listener)
        )
    }

    //直接下载，显示进度，4G下载看参数，下载完成自动安装且弹框
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
        DownloadFile.startDownloadWithProcess(
                activity,
                title, msg,
                url, "", md5,
                canCancel, true, downloadMobile,
                listener,
                SimpleInstallListener(activity, packageName, listener)
        )
    }

    //直接下载，不显示进度，4G下载弹框，下载完成自动安装且弹框
    fun startDownloadWithCheck(activity: Activity, url: String, md5: String, packageName: String) {
        DownloadFile.startDownloadWithCheckAndProcess(
                activity,
                "", "", "",
                url, md5,
                canCancel = true, useProcess = false,
                listener = null,
                downloadListener = object : SimpleDownloadListener() {
                    override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                    }

                    override fun onComplete(filePath: String, item: DownloadItem) {
                        ThreadManager.getInstance().runOnUIThread {
                            InstallUtils.installAPP(activity, filePath, packageName)
                        }
                    }

                    override fun onProgress(item: DownloadItem) {
                    }

                })
    }

    //直接下载，不显示进度，4G下载不弹框直接下载，下载完成自动安装
    fun startDownload(context: Context, url: String, md5: String, packageName: String) {
        DownloadFile.startDownload(
                context,
                "", "",
                url, "", md5, forceDownload = false,
                canPart = true, UseMobile = true, downloadListener = object : SimpleDownloadListener() {
            override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
            }

            override fun onComplete(filePath: String, item: DownloadItem) {
                ThreadManager.getInstance().runOnUIThread {
                    InstallUtils.installAPP(context, filePath, packageName)
                }
            }

            override fun onProgress(item: DownloadItem) {
            }

        })
    }
}