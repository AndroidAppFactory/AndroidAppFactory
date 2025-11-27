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
 * 通用文件下载工具类
 *
 * 提供灵活的文件下载功能，支持多种下载场景和配置：
 *
 * 核心功能：
 * - 网络类型检测（WiFi/4G 自动识别）
 * - 4G 网络下载提示（可配置）
 * - 下载进度展示（可选进度对话框）
 * - MD5/SHA256 文件校验
 * - 断点续传支持
 * - 自定义下载路径
 * - 下载队列管理
 *
 * 下载模式：
 * 1. **download**: 普通下载，队列满时等待
 * 2. **forceDownload**: 强制下载，队列满时也立即开始
 *
 * 网络策略：
 * - downloadWithCheck: 4G 网络弹框提示用户
 * - download/forceDownload: 根据参数控制 4G 下载行为
 *
 * 进度展示：
 * - downloadWithProcess: 显示进度对话框
 * - download/forceDownload: 后台静默下载
 *
 * 使用场景：
 * 1. 大文件下载（视频、压缩包等）
 * 2. 资源包下载（游戏资源、素材包等）
 * 3. 文档下载（PDF、Office 文档等）
 * 4. 媒体文件下载（图片、音频等）
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-01-10.
 * Description: 通用文件下载工具类，提供丰富的下载配置和灵活的使用方式
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