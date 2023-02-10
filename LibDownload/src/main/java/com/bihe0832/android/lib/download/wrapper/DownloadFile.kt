package com.bihe0832.android.lib.download.wrapper

import android.app.Activity
import android.content.Context
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadListener
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.network.NetworkUtil
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.DownloadProgressDialog
import com.bihe0832.android.lib.ui.dialog.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.impl.DialogUtils
import com.bihe0832.android.lib.ui.dialog.impl.SimpleDialogListener
import com.bihe0832.android.lib.ui.toast.ToastUtil

/**
 * forceDownload 与 downlod 区别：当下载队列已满，是否继续直接下载，还是等待队列任务完成再下载
 *
 */

object DownloadFile {

    //检测网络类型，并且4G弹框，不使用进度条
    fun downloadWithCheck(activity: Activity, url: String, downloadListener: DownloadListener?) {
        downloadWithCheck(activity, url, "", downloadListener)
    }

    //检测网络类型，并且4G弹框，不使用进度条
    fun downloadWithCheck(activity: Activity, url: String, md5: String, downloadListener: DownloadListener?) {
        downloadWithCheck(activity, url, "", false, md5, true, downloadListener)
    }

    //检测网络类型，并且4G弹框，不使用进度条
    fun downloadWithCheck(activity: Activity, url: String, path: String, isFile: Boolean, md5: String, canCancel: Boolean, downloadListener: DownloadListener?) {
        downloadWithCheckAndProcess(activity, url, path, isFile, md5, canCancel, null, downloadListener)
    }

    fun downloadWithCheckAndProcess(activity: Activity, url: String, path: String, isFile: Boolean, md5: String, canCancel: Boolean, listener: OnDialogListener?, downloadListener: DownloadListener?) {
        downloadWithCheckAndProcess(activity, "", "", url, path, isFile, md5, "", canCancel, useProcess = false, forceDownload = false, listener = listener, downloadListener = downloadListener)
    }

    fun downloadWithCheckAndProcess(activity: Activity, title: String, msg: String, url: String, path: String, isFile: Boolean, md5: String, canCancel: Boolean, listener: OnDialogListener?, downloadListener: DownloadListener?) {
        downloadWithCheckAndProcess(activity, title, msg, url, path, isFile, md5, "", canCancel, true, forceDownload = false, listener = listener, downloadListener = downloadListener)
    }

    //检测网络类型，并且4G弹框，进度条参数控制
    fun downloadWithCheckAndProcess(activity: Activity, title: String, msg: String, url: String, path: String, isFile: Boolean, md5: String, sha256: String, canCancel: Boolean, useProcess: Boolean, forceDownload: Boolean, listener: OnDialogListener?, downloadListener: DownloadListener?) {
        if (null == activity || url.isNullOrBlank()) {
            return
        }
        if (NetworkUtil.isNetworkConnected(activity)) {
            if (NetworkUtil.isMobileNet(activity)) {
                DialogUtils.showConfirmDialog(activity, "移动网络下载提示", "当前处于移动网络, 下载将消耗流量，是否继续下载?", "继续下载", "稍候下载", canCancel, object : SimpleDialogListener() {
                    override fun onPositiveClick() {
                        if (useProcess) {
                            downloadWithProcess(activity, title, msg, url, path, isFile, md5, sha256, canCancel, forceDownloadNew = false, useMobile = true, forceDownload = forceDownload, listener = listener, downloadListener = downloadListener)
                        } else {
                            startDownload(activity, title, msg, url, path, isFile, md5, sha256, forceDownloadNew = false, UseMobile = true, forceDownload = forceDownload, downloadListener = downloadListener)
                        }
                    }
                })
            } else {
                if (useProcess) {
                    downloadWithProcess(activity, title, msg, url, path, isFile, md5, sha256, canCancel, forceDownloadNew = false, useMobile = true, forceDownload = forceDownload, listener = listener, downloadListener = downloadListener)
                } else {
                    startDownload(activity, title, msg, url, "", isFile, md5, sha256, forceDownloadNew = false, UseMobile = true, forceDownload = forceDownload, downloadListener = downloadListener)
                }
            }
        } else {
            ToastUtil.showShort(activity, "网络已经断开，请先检查网络")
        }
    }

    //显示进度条
    fun downloadWithProcess(activity: Activity, title: String, msg: String, url: String, path: String, isFile: Boolean, md5: String, sha256: String, canCancel: Boolean, forceDownloadNew: Boolean, useMobile: Boolean, forceDownload: Boolean, listener: OnDialogListener?, downloadListener: DownloadListener?) {
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
                    DownloadUtils.deleteTask(DownloadUtils.getDownloadIDByURL(url), true)
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
        startDownload(activity.applicationContext, title, msg, url, path, isFile, md5, sha256, forceDownloadNew, useMobile, forceDownload, object : DownloadListener {
            override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                ToastUtil.showShort(activity, "应用下载失败（$errorCode）")
                ThreadManager.getInstance().start({
                    ThreadManager.getInstance().runOnUIThread {
                        progressDialog.dismiss()
                    }
                }, 2)
                downloadListener?.onFail(errorCode, msg, item)
            }

            override fun onComplete(filePath: String, item: DownloadItem): String {
                ZLog.i("startDownload download Path: $filePath")
                ThreadManager.getInstance().start({
                    ThreadManager.getInstance().runOnUIThread {
                        progressDialog.dismiss()
                    }
                }, 2)
                return downloadListener?.onComplete(filePath, item) ?: filePath
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
    fun forceDownload(context: Context, url: String, forceDownloadNew: Boolean, downloadListener: DownloadListener?) {
        forceDownload(context, "", "", url, "", false, "", "", forceDownloadNew, UseMobile = true, downloadListener = downloadListener)
    }

    fun download(context: Context, url: String, forceDownloadNew: Boolean, downloadListener: DownloadListener?) {
        download(context, "", "", url, "", false, "", "", forceDownloadNew, UseMobile = true, downloadListener = downloadListener)
    }

    //不检测网络类型，4G自动下载，不使用进度条
    fun forceDownload(context: Context, url: String, downloadListener: DownloadListener?) {
        forceDownload(context, url, "", false, downloadListener)
    }

    fun download(context: Context, url: String, downloadListener: DownloadListener?) {
        download(context, url, "", false, downloadListener)
    }

    fun forceDownload(context: Context, url: String, path: String, isFile: Boolean, downloadListener: DownloadListener?) {
        forceDownload(context, url, path, isFile, "", downloadListener)
    }

    fun download(context: Context, url: String, path: String, isFile: Boolean, downloadListener: DownloadListener?) {
        download(context, url, path, isFile, "", downloadListener)
    }

    //不检测网络类型，4G自动下载，不使用进度条
    fun forceDownload(context: Context, url: String, path: String, isFile: Boolean, md5: String, downloadListener: DownloadListener?) {
        forceDownload(context, "", "", url, path, isFile, md5, "", forceDownloadNew = false, UseMobile = true, downloadListener = downloadListener)
    }

    fun download(context: Context, url: String, path: String, isFile: Boolean, md5: String, downloadListener: DownloadListener?) {
        download(context, "", "", url, path, isFile, md5, "", forceDownloadNew = false, UseMobile = true, downloadListener = downloadListener)
    }

    //不检测网络类型，4G下载参数控制，不使用进度条
    fun download(context: Context, url: String, path: String, isFile: Boolean, md5: String, useMobile: Boolean, downloadListener: DownloadListener?) {
        download(context, "", "", url, path, isFile, md5, "", forceDownloadNew = false, UseMobile = useMobile, downloadListener = downloadListener)
    }

    fun download(context: Context, title: String, msg: String, url: String, path: String, isFile: Boolean, md5: String, sha256: String, forceDownloadNew: Boolean, UseMobile: Boolean, downloadListener: DownloadListener?) {
        startDownload(context, title, msg, url, path, isFile, md5, sha256, forceDownloadNew, UseMobile, false, downloadListener)
    }

    fun forceDownload(context: Context, title: String, msg: String, url: String, path: String, isFile: Boolean, md5: String, sha256: String, forceDownloadNew: Boolean, UseMobile: Boolean, downloadListener: DownloadListener?) {
        startDownload(context, title, msg, url, path, isFile, md5, sha256, forceDownloadNew, UseMobile, true, downloadListener)
    }

    private fun startDownload(context: Context, title: String, msg: String, url: String, path: String, isFile: Boolean, md5: String, sha256: String, forceDownloadNew: Boolean, UseMobile: Boolean, forceDownload: Boolean, downloadListener: DownloadListener?) {
        DownloadTools.startDownload(context, title, msg, url, path, isFile, md5, sha256, forceDownloadNew, UseMobile, "DownloadFile", forceDownload, downloadListener)
    }
}