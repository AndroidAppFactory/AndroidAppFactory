package com.bihe0832.android.lib.download.wrapper

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadListener
import com.bihe0832.android.lib.install.InstallUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.network.NetworkUtil
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.CommonDialog
import com.bihe0832.android.lib.ui.dialog.DownloadProgressDialog
import com.bihe0832.android.lib.ui.dialog.OnDialogListener
import com.bihe0832.android.lib.ui.toast.ToastUtil


object DownloadFile {

    //检测网络类型，并且4G弹框，不使用进度条
    fun startDownloadWithCheck(activity: Activity, url: String, downloadListener: DownloadListener?) {
        startDownloadWithCheck(activity, url, "", false, downloadListener)
    }

    //检测网络类型，并且4G弹框，不使用进度条
    fun startDownloadWithCheck(activity: Activity, url: String, md5: String, downloadListener: DownloadListener?) {
        startDownloadWithCheck(activity, url, md5, false, downloadListener)
    }

    //检测网络类型，并且4G弹框，不使用进度条
    fun startDownloadWithCheck(activity: Activity, url: String, md5: String, canCancel: Boolean, downloadListener: DownloadListener?) {
        startDownloadWithCheckAndProcess(activity, url, md5, canCancel, null, downloadListener)
    }

    //检测网络类型，并且4G弹框，不显示进度条
    fun startDownloadWithCheckAndProcess(activity: Activity, url: String, md5: String, canCancle: Boolean, listener: OnDialogListener?, downloadListener: DownloadListener?) {
        startDownloadWithCheckAndProcess(activity, url, "", md5, canCancle, listener, downloadListener)
    }

    fun startDownloadWithCheckAndProcess(activity: Activity, url: String, filePath: String, md5: String, canCancle: Boolean, listener: OnDialogListener?, downloadListener: DownloadListener?) {
        startDownloadWithCheckAndProcess(activity, "", "", url, filePath, md5, canCancle, false, listener, downloadListener)
    }


    //检测网络类型，并且4G弹框，显示进度条
    fun startDownloadWithCheckAndProcess(activity: Activity, title: String, msg: String, url: String, md5: String, canCancel: Boolean, listener: OnDialogListener?, downloadListener: DownloadListener?) {
        startDownloadWithCheckAndProcess(activity, title, msg, url, "", md5, canCancel, listener, downloadListener)
    }

    fun startDownloadWithCheckAndProcess(activity: Activity, title: String, msg: String, url: String, filePath: String, md5: String, canCancel: Boolean, listener: OnDialogListener?, downloadListener: DownloadListener?) {
        startDownloadWithCheckAndProcess(activity, title, msg, url, filePath, md5, canCancel, true, listener, downloadListener)
    }

    //检测网络类型，并且4G弹框，进度条参数控制
    fun startDownloadWithCheckAndProcess(activity: Activity, title: String, msg: String, url: String, filePath: String, md5: String, canCancel: Boolean, useProcess: Boolean, listener: OnDialogListener?, downloadListener: DownloadListener?) {
        if (null == activity || url.isNullOrBlank()) {
            return
        }
        if (NetworkUtil.isNetworkConnected(activity)) {
            if (NetworkUtil.isMobileNet(activity)) {
                CommonDialog(activity).apply {
                    this.title = "移动网络下载提示"
                    content = "当前处于移动网络, 下载将消耗流量，是否继续下载?"
                    negative = "稍候下载"
                    positive = "继续下载"
                    setShouldCanceled(canCancel)
                    setOnClickBottomListener(object : OnDialogListener {
                        override fun onPositiveClick() {
                            dismiss()
                            if (useProcess) {
                                startDownloadWithProcess(activity, title, msg, url, filePath, md5, canCancel, true, listener, downloadListener)
                            } else {
                                startDownload(activity, title, msg, url, filePath, md5, true, true, downloadListener)
                            }
                        }

                        override fun onNegativeClick() {
                            dismiss()
                        }

                        override fun onCancel() {
                            dismiss()
                        }
                    })
                }.let {
                    it.show()
                }
            } else {
                if (useProcess) {
                    startDownloadWithProcess(activity, title, msg, url, filePath, md5, canCancel, true, listener, downloadListener)
                } else {
                    startDownload(activity, title, msg, url, "", md5, true, true, downloadListener)
                }
            }
        } else {
            ToastUtil.showShort(activity, "网络已经断开，请先检查网络")
        }
    }

    //显示进度条
    fun startDownloadWithProcess(activity: Activity, title: String, msg: String, url: String, filePath: String, md5: String, canCancel: Boolean, useMobile: Boolean, listener: OnDialogListener?, downloadListener: DownloadListener?) {
        var progressDialog = DownloadProgressDialog(activity).apply {
            setTitle(title)
            setMessage(msg)
            setCurrentSize(0)
            setShouldCanceled(canCancel)
            if (canCancel) {
                setPositive("后台下载")
                setNegative("取消下载")
            } else {
                setPositive("取消下载")
            }

            setOnClickListener(object : OnDialogListener {
                override fun onPositiveClick() {
                    if (canCancel) {
                        ToastUtil.showShort(activity, "已切换到后台下载，你可以在通知栏查看下载进度")
                    }
                    dismiss()
                    listener?.onPositiveClick()
                }

                override fun onNegativeClick() {
                    DownloadUtils.deleteTask(DownloadItem.getDownloadIDByURL(url), true)
                    dismiss()
                    listener?.onNegativeClick()
                }

                override fun onCancel() {
                    if (canCancel) {
                        ToastUtil.showShort(activity, "已切换到后台下载，你可以在通知栏查看下载进度")
                    }
                    dismiss()
                    listener?.onCancel()
                }
            })
        }
        ThreadManager.getInstance().runOnUIThread { progressDialog.show() }
        startDownload(activity.applicationContext, title, msg, url, filePath, md5, true, useMobile, object : DownloadListener {
            override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                ToastUtil.showShort(activity, "应用下载失败（$errorCode）")
                ThreadManager.getInstance().runOnUIThread {
                    progressDialog.dismiss()
                }
                downloadListener?.onFail(errorCode, msg, item)
            }

            override fun onComplete(filePath: String, item: DownloadItem) {
                ZLog.i("startDownloadApk download installApkPath: $filePath")
                ThreadManager.getInstance().runOnUIThread {
                    progressDialog.dismiss()
                }
                downloadListener?.onComplete(filePath, item)
            }

            override fun onDelete(item: DownloadItem) {
                downloadListener?.onDelete(item)
                ThreadManager.getInstance().runOnUIThread {
                    progressDialog.dismiss()
                }
            }

            override fun onWait(item: DownloadItem) {
                downloadListener?.onWait(item)
            }

            override fun onStart(item: DownloadItem) {
                downloadListener?.onWait(item)
            }

            override fun onProgress(item: DownloadItem) {
                activity.runOnUiThread(Runnable {
                    progressDialog.setAPKSize(item.fileLength)
                    progressDialog.setCurrentSize(item.finished)
                })
                downloadListener?.onProgress(item)
            }

            override fun onPause(item: DownloadItem) {
                ThreadManager.getInstance().runOnUIThread {
                    progressDialog.dismiss()
                }
                downloadListener?.onPause(item)
            }
        })
    }

    //不检测网络类型，4G自动下载，不使用进度条
    fun startDownload(context: Context, url: String, downloadListener: DownloadListener?) {
        startDownload(context, url, "", downloadListener)
    }

    fun startDownload(context: Context, url: String, filePath: String, downloadListener: DownloadListener?) {
        startDownload(context, url, filePath, "", downloadListener)
    }


    //不检测网络类型，4G自动下载，不使用进度条
    fun startDownload(context: Context, url: String, filePath: String, md5: String, downloadListener: DownloadListener?) {
        startDownload(context, url, filePath, md5, true, downloadListener)
    }

    //不检测网络类型，4G下载参数控制，不使用进度条
    fun startDownload(context: Context, url: String, filePath: String, md5: String, useMobile: Boolean, downloadListener: DownloadListener?) {
        startDownload(context, "", "", url, filePath, md5, false, useMobile, downloadListener)
    }

    fun startDownload(context: Context, title: String, msg: String, url: String, filePath: String, md5: String, canPart: Boolean, UseMobile: Boolean, downloadListener: DownloadListener?) {
        DownloadUtils.startDownload(context, DownloadItem().apply {
            if (InstallUtils.isApkFile(URLUtils.getFileName(url))) {
                setNotificationVisibility(true)
            } else {
                setNotificationVisibility(false)
            }
            downloadURL = url
            downloadTitle = title
            downloadDesc = msg
            fileMD5 = md5
            if (!TextUtils.isEmpty(filePath)) {
                fileNameWithPath = filePath
            }

            isDownloadWhenUseMobile = UseMobile
            setCanDownloadByPart(canPart)
            this.downloadListener = downloadListener
        }, true)
    }
}