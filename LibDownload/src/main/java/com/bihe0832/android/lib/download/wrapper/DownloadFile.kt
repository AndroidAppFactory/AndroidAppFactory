package com.bihe0832.android.lib.download.wrapper

import android.app.Activity
import android.content.Context
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadListener
import com.bihe0832.android.lib.download.R
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.network.NetworkUtil
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.impl.DownloadProgressDialog
import com.bihe0832.android.lib.ui.dialog.tools.DialogUtils
import com.bihe0832.android.lib.ui.dialog.tools.SimpleDialogListener
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
    fun downloadWithCheck(
        activity: Activity,
        url: String,
        md5: String,
        downloadListener: DownloadListener?
    ) {
        downloadWithCheck(activity, url, "", false, md5, true, downloadListener)
    }

    //检测网络类型，并且4G弹框，不使用进度条
    fun downloadWithCheck(
        activity: Activity,
        url: String,
        path: String,
        isFile: Boolean,
        md5: String,
        canCancel: Boolean,
        downloadListener: DownloadListener?
    ) {
        downloadWithCheckAndProcess(
            activity,
            url,
            emptyMap(),
            path,
            isFile,
            md5,
            canCancel,
            null,
            downloadListener
        )
    }

    fun downloadWithCheckAndProcess(
        activity: Activity,
        url: String,
        header: Map<String, String>,
        path: String,
        isFile: Boolean,
        md5: String,
        canCancel: Boolean,
        listener: OnDialogListener?,
        downloadListener: DownloadListener?
    ) {
        downloadWithCheckAndProcess(
            activity,
            "",
            "",
            url,
            header,
            path,
            isFile,
            md5,
            "",
            canCancel,
            useProcess = false,
            forceDownload = false,
            needRecord = false,
            listener = listener,
            downloadListener = downloadListener
        )
    }

    fun downloadWithCheckAndProcess(
        activity: Activity,
        title: String,
        msg: String,
        url: String,
        path: String,
        isFile: Boolean,
        md5: String,
        canCancel: Boolean,
        listener: OnDialogListener?,
        downloadListener: DownloadListener?
    ) {
        downloadWithCheckAndProcess(
            activity,
            title,
            msg,
            url,
            emptyMap(),
            path,
            isFile,
            md5,
            "",
            canCancel,
            true,
            forceDownload = false,
            needRecord = false,
            listener = listener,
            downloadListener = downloadListener
        )
    }

    //检测网络类型，并且4G弹框，进度条参数控制
    fun downloadWithCheckAndProcess(
        activity: Activity,
        title: String,
        msg: String,
        url: String,
        header: Map<String, String>,
        path: String,
        isFile: Boolean,
        md5: String,
        sha256: String,
        canCancel: Boolean,
        useProcess: Boolean,
        forceDownload: Boolean,
        needRecord: Boolean,
        listener: OnDialogListener?,
        downloadListener: DownloadListener?
    ) {
        if (null == activity || url.isNullOrBlank()) {
            return
        }
        if (NetworkUtil.isNetworkConnected(activity)) {
            if (NetworkUtil.isMobileNet(activity)) {
                DialogUtils.showConfirmDialog(activity,
                    activity.getString(R.string.download_dialog_mobile_title),
                    activity.getString(R.string.download_dialog_mobile_desc),
                    activity.getString(R.string.download_dialog_mobile_positive),
                    activity.getString(R.string.download_dialog_mobile_negative),
                    canCancel,
                    object : SimpleDialogListener() {
                        override fun onPositiveClick() {
                            if (useProcess) {
                                downloadWithProcess(
                                    activity,
                                    title,
                                    msg,
                                    url,
                                    header,
                                    path,
                                    isFile,
                                    md5,
                                    sha256,
                                    canCancel,
                                    forceDownloadNew = false,
                                    useMobile = true,
                                    forceDownload = forceDownload,
                                    needRecord = needRecord,
                                    listener = listener,
                                    downloadListener = downloadListener
                                )
                            } else {
                                startDownload(
                                    activity,
                                    title,
                                    msg,
                                    url,
                                    header,
                                    path,
                                    isFile,
                                    md5,
                                    sha256,
                                    forceDownloadNew = false,
                                    useMobile = true,
                                    forceDownload = forceDownload,
                                    needRecord = needRecord,
                                    downloadListener = downloadListener
                                )
                            }
                        }
                    })
            } else {
                if (useProcess) {
                    downloadWithProcess(
                        activity,
                        title,
                        msg,
                        url,
                        header,
                        path,
                        isFile,
                        md5,
                        sha256,
                        canCancel,
                        forceDownloadNew = false,
                        useMobile = true,
                        forceDownload = forceDownload,
                        needRecord = needRecord,
                        listener = listener,
                        downloadListener = downloadListener
                    )
                } else {
                    startDownload(
                        activity,
                        title,
                        msg,
                        url,
                        header,
                        "",
                        isFile,
                        md5,
                        sha256,
                        forceDownloadNew = false,
                        useMobile = true,
                        forceDownload = forceDownload,
                        needRecord = needRecord,
                        downloadListener = downloadListener
                    )
                }
            }
        } else {
            ToastUtil.showShort(activity, activity.getString(R.string.download_network_bad))
        }
    }

    //显示进度条
    fun downloadWithProcess(
        activity: Activity,
        title: String,
        msg: String,
        url: String,
        header: Map<String, String>,
        path: String,
        isFile: Boolean,
        md5: String,
        sha256: String,
        canCancel: Boolean,
        forceDownloadNew: Boolean,
        useMobile: Boolean,
        forceDownload: Boolean,
        needRecord: Boolean,
        listener: OnDialogListener?,
        downloadListener: DownloadListener?
    ) {
        val progressDialog = DownloadProgressDialog(activity).apply {
            setPercentScale(3)
            setTitle(title)
            setMessage(msg)
            setCurrentSize(0, 0)
            setShouldCanceled(canCancel)
            if (canCancel) {
                setPositive(activity.getString(R.string.download_background_downloading))
                setNegative(activity.getString(R.string.download_cancel))
            } else {
                setPositive(activity.getString(R.string.download_cancel))
            }

            setOnClickListener(object : OnDialogListener {
                override fun onPositiveClick() {
                    if (canCancel) {
                        ToastUtil.showShort(
                            activity,
                            activity.getString(R.string.download_background)
                        )
                    }
                    dismiss()
                    listener?.onPositiveClick()
                }

                override fun onNegativeClick() {
                    dismiss()
                    DownloadFileUtils.deleteTask(
                        DownloadFileUtils.getDownloadIDByURL(url), true
                    )
                    listener?.onNegativeClick()
                }

                override fun onCancel() {
                    if (canCancel) {
                        ToastUtil.showShort(
                            activity,
                            activity.getString(R.string.download_background)
                        )
                    }
                    dismiss()
                    listener?.onCancel()
                }
            })
        }
        ThreadManager.getInstance().runOnUIThread { progressDialog.show() }
        startDownload(activity.applicationContext,
            title,
            msg,
            url,
            header,
            path,
            isFile,
            md5,
            sha256,
            forceDownloadNew,
            useMobile,
            forceDownload,
            needRecord,
            object : DownloadListener {
                fun updateProcess(item: DownloadItem) {
                    activity.runOnUiThread {
                        progressDialog.setContentSize(item.contentLength)
                        progressDialog.setCurrentSize(item.finished - 1, item.lastSpeed)
                    }
                }

                override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                    ToastUtil.showShort(
                        activity,
                        activity.getString(R.string.download_failed) + "（$errorCode）"
                    )
                    ThreadManager.getInstance().start({
                        downloadListener?.onFail(errorCode, msg, item)
                        ThreadManager.getInstance().runOnUIThread {
                            progressDialog.dismiss()
                        }
                    }, 2)
                }

                override fun onComplete(filePath: String, item: DownloadItem): String {
                    ZLog.i("startDownload download Path: $filePath")
                    val result = downloadListener?.onComplete(filePath, item) ?: filePath
                    ThreadManager.getInstance().runOnUIThread {
                        progressDialog.setCurrentSize(item.contentLength, item.lastSpeed)
                        progressDialog.dismiss()
                    }
                    return result
                }

                override fun onDelete(item: DownloadItem) {
                    downloadListener?.onDelete(item)
                    ThreadManager.getInstance().runOnUIThread {
                        progressDialog.dismiss()
                    }
                }

                override fun onWait(item: DownloadItem) {
                    downloadListener?.onWait(item)
                    updateProcess(item)
                }

                override fun onStart(item: DownloadItem) {
                    downloadListener?.onWait(item)
                    updateProcess(item)
                }

                override fun onProgress(item: DownloadItem) {
                    updateProcess(item)
                    downloadListener?.onProgress(item)
                }

                override fun onPause(item: DownloadItem) {
                    downloadListener?.onPause(item)
                    ThreadManager.getInstance().runOnUIThread {
                        progressDialog.dismiss()
                    }
                }
            })
    }

    //不检测网络类型，4G自动下载，不使用进度条
    fun forceDownload(
        context: Context,
        url: String,
        forceDownloadNew: Boolean,
        downloadListener: DownloadListener?
    ) {
        forceDownload(
            context,
            "",
            "",
            url,
            emptyMap(),
            "",
            false,
            "",
            "",
            forceDownloadNew,
            UseMobile = true,
            downloadListener = downloadListener
        )
    }

    fun download(
        context: Context,
        url: String,
        forceDownloadNew: Boolean,
        downloadListener: DownloadListener?
    ) {
        download(
            context,
            "",
            "",
            url,
            emptyMap(),
            "",
            false,
            "",
            "",
            forceDownloadNew,
            UseMobile = true,
            downloadListener = downloadListener
        )
    }

    //不检测网络类型，4G自动下载，不使用进度条
    fun forceDownload(context: Context, url: String, downloadListener: DownloadListener?) {
        forceDownload(context, url, "", false, downloadListener)
    }

    fun download(context: Context, url: String, downloadListener: DownloadListener?) {
        download(context, url, "", false, downloadListener)
    }

    fun forceDownload(
        context: Context,
        url: String,
        path: String,
        isFile: Boolean,
        downloadListener: DownloadListener?
    ) {
        forceDownload(context, url, path, isFile, "", downloadListener)
    }

    fun download(
        context: Context,
        url: String,
        path: String,
        isFile: Boolean,
        downloadListener: DownloadListener?
    ) {
        download(context, url, path, isFile, "", downloadListener)
    }

    //不检测网络类型，4G自动下载，不使用进度条
    fun forceDownload(
        context: Context,
        url: String,
        path: String,
        isFile: Boolean,
        md5: String,
        downloadListener: DownloadListener?
    ) {
        forceDownload(
            context,
            "",
            "",
            url,
            emptyMap(),
            path,
            isFile,
            md5,
            "",
            forceDownloadNew = false,
            UseMobile = true,
            downloadListener = downloadListener
        )
    }

    fun download(
        context: Context,
        url: String,
        path: String,
        isFile: Boolean,
        md5: String,
        downloadListener: DownloadListener?
    ) {
        download(
            context,
            "",
            "",
            url,
            emptyMap(),
            path,
            isFile,
            md5,
            "",
            forceDownloadNew = false,
            UseMobile = true,
            downloadListener = downloadListener
        )
    }

    //不检测网络类型，4G下载参数控制，不使用进度条
    fun download(
        context: Context,
        url: String,
        path: String,
        isFile: Boolean,
        md5: String,
        useMobile: Boolean,
        downloadListener: DownloadListener?
    ) {
        download(
            context,
            "",
            "",
            url,
            emptyMap(),
            path,
            isFile,
            md5,
            "",
            forceDownloadNew = false,
            UseMobile = useMobile,
            downloadListener = downloadListener
        )
    }

    fun download(
        context: Context,
        title: String,
        msg: String,
        url: String,
        header: Map<String, String>,
        path: String,
        isFile: Boolean,
        md5: String,
        sha256: String,
        forceDownloadNew: Boolean,
        UseMobile: Boolean,
        downloadListener: DownloadListener?
    ) {
        startDownload(
            context,
            title,
            msg,
            url,
            header,
            path,
            isFile,
            md5,
            sha256,
            forceDownloadNew,
            UseMobile,
            forceDownload = false,
            needRecord = false,
            downloadListener = downloadListener
        )
    }

    fun forceDownload(
        context: Context,
        title: String,
        msg: String,
        url: String,
        header: Map<String, String>,
        path: String,
        isFile: Boolean,
        md5: String,
        sha256: String,
        forceDownloadNew: Boolean,
        UseMobile: Boolean,
        downloadListener: DownloadListener?
    ) {
        startDownload(
            context,
            title,
            msg,
            url,
            header,
            path,
            isFile,
            md5,
            sha256,
            forceDownloadNew,
            UseMobile,
            forceDownload = true,
            needRecord = false,
            downloadListener = downloadListener
        )
    }

    private fun startDownload(
        context: Context,
        title: String,
        msg: String,
        url: String,
        header: Map<String, String>,
        path: String,
        isFile: Boolean,
        md5: String,
        sha256: String,
        forceDownloadNew: Boolean,
        useMobile: Boolean,
        forceDownload: Boolean,
        needRecord: Boolean,
        downloadListener: DownloadListener?
    ) {
        DownloadTools.startDownload(
            context,
            title,
            msg,
            url,
            header,
            path,
            isFile,
            md5,
            sha256,
            forceDownloadNew,
            useMobile,
            DownloadFileUtils.DOWNLOAD_ACTION_KEY_FILE,
            forceDownload,
            needRecord,
            downloadListener
        )
    }
}