package com.bihe0832.android.lib.download.wrapper

import android.app.Activity
import android.content.Context
import android.provider.Settings
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadListener
import com.bihe0832.android.lib.download.R
import com.bihe0832.android.lib.aaf.res.R as ResR
import com.bihe0832.android.lib.install.InstallListener
import com.bihe0832.android.lib.install.InstallUtils
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.tools.DialogUtils
import com.bihe0832.android.lib.utils.intent.IntentUtils

/**
 * APK 下载工具类
 *
 * 专门用于 APK 文件的下载和安装场景，提供以下功能：
 * - APK 下载前的网络检查（4G 提示）
 * - 下载进度展示（可选）
 * - 下载完成后自动安装
 * - 安装权限检查和引导
 * - MD5 校验支持
 * - 支持强制下载和顺序下载
 *
 * 使用场景：
 * 1. 应用内更新：下载新版本 APK 并自动安装
 * 2. 插件下载：下载并安装插件 APK
 * 3. 批量应用安装：下载多个 APK 并依次安装
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-01-10.
 * Description: APK 下载和安装的便捷工具类，封装了下载、校验、安装的完整流程
 */
object DownloadAPK {

    fun showInstallPermissionDialog(
        activity: Activity,
        title: String,
        listener: OnDialogListener?
    ) {
        DialogUtils.showConfirmDialog(
            activity,
            title,
            activity.getString(ResR.string.download_background_permission),
            activity.getString(ResR.string.com_bihe0832_permission_positive),
            activity.getString(ResR.string.com_bihe0832_permission_negtive),
            false,
            object : OnDialogListener {
                override fun onPositiveClick() {
                    IntentUtils.startAppSettings(
                        activity,
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES
                    )
                    listener?.onPositiveClick()
                }

                override fun onNegativeClick() {
                    listener?.onNegativeClick()
                }

                override fun onCancel() {
                    listener?.onCancel()
                }
            },
        )
    }

    //直接下载，不显示进度，4G下载弹框，下载完成自动安装且弹框
    fun startDownloadWithCheckAndProcess(activity: Activity, url: String) {
        startDownloadWithCheck(activity, url, "")
    }


    //直接下载，显示进度，可以取消，4G下载弹框，下载完成自动安装且弹框
    fun startDownloadWithCheckAndProcess(
        activity: Activity,
        title: String,
        msg: String,
        url: String,
        md5: String,
        packageName: String,
    ) {
        startDownloadWithCheckAndProcess(
            activity,
            title, msg,
            url, emptyMap(), md5,
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
        header: Map<String, String>,
        md5: String,
        packageName: String,
        canCancel: Boolean,
        listener: OnDialogListener?,
    ) {
        DownloadFile.downloadWithCheckAndProcess(
            activity,
            title, msg,
            url, header, "", false, md5, "",
            canCancel,
            true,
            forceDownload = false,
            needRecord = false,
            object : OnDialogListener {
                override fun onPositiveClick() {
                    listener?.onPositiveClick()
                }

                override fun onNegativeClick() {
                    listener?.onNegativeClick()
                }

                override fun onCancel() {
                    listener?.onCancel()
                }

            },
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
        versionCode: Long,
        canCancel: Boolean,
        downloadMobile: Boolean,
        listener: OnDialogListener?,
    ) {
        startDownloadWithProcess(
            activity,
            title, msg,
            url, emptyMap(), md5, packageName, versionCode,
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
        header: Map<String, String>,
        md5: String,
        packageName: String,
        versionCode: Long,
        canCancel: Boolean,
        downloadMobile: Boolean,
        listener: OnDialogListener?,
        downloadListener: DownloadListener?,
    ) {
        DownloadFile.downloadWithProcess(
            activity,
            title,
            msg,
            url,
            header,
            "",
            false,
            md5,
            "",
            packageName,
            versionCode,
            canCancel,
            forceDownloadNew = false,
            downloadMobile,
            forceDownload = true,
            needRecord = false,
            object : OnDialogListener {
                override fun onPositiveClick() {
                    if (!InstallUtils.hasInstallAPPPermission(activity, false, false)) {
                        showInstallPermissionDialog(activity, title, listener)
                    } else {
                        listener?.onPositiveClick()
                    }
                }

                override fun onNegativeClick() {
                    listener?.onNegativeClick()
                }

                override fun onCancel() {
                    listener?.onCancel()
                }

            },
            downloadListener
        )
    }

    //直接下载，显示进度，4G下载弹框，强制下载，下载完成自动安装且弹框
    fun startDownloadWithCheck(activity: Activity, url: String, md5: String) {
        DownloadFile.downloadWithCheckAndProcess(
            activity,
            "", "",
            url, emptyMap(), "", false, md5, "",
            canCancel = true, useProcess = false,
            forceDownload = true, needRecord = false,
            listener = object : OnDialogListener {
                override fun onPositiveClick() {
                    if (!InstallUtils.hasInstallAPPPermission(activity, false, false)) {
                        showInstallPermissionDialog(
                            activity,
                            activity.getString(ResR.string.dialog_title),
                            null
                        )
                    }
                }

                override fun onNegativeClick() {

                }

                override fun onCancel() {

                }
            },
            downloadListener = SimpleAPKDownloadListener(activity)
        )
    }

    private class SimpleAPKDownloadListener(
        private val context: Context,
        private val packageName: String = "",
        private val listener: InstallListener? = null
    ) : SimpleDownloadListener() {
        override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
        }

        override fun onComplete(filePath: String, item: DownloadItem): String {
            ThreadManager.getInstance().runOnUIThread {
                InstallUtils.installAPP(context, filePath, packageName, listener)
            }
            return filePath
        }

        override fun onProgress(item: DownloadItem) {
        }

    }

    //直接下载，不显示进度，4G下载不弹框直接下载，下载完成自动安装
    fun forceDownload(
        context: Context,
        url: String,
        md5: String,
        packageName: String,
        versionCode: Long,
        delay: Int,
        installListener: InstallListener
    ) {
        DownloadTools.startDownload(
            context = context,
            title = "",
            msg = "",
            url = url,
            header = emptyMap(),
            path = "",
            isFilePath = false,
            md5 = md5,
            sha256 = "",
            packageName = packageName,
            versionCode = versionCode,
            forceDownloadNew = true,
            useMobile = true,
            actionKey = DownloadFileUtils.DOWNLOAD_ACTION_KEY_APK,
            forceDownload = false,
            needRecord = false,
            downloadListener = SimpleAPKDownloadListener(
                context,
                packageName,
                installListener
            )
        )
    }

    fun download(
        context: Context,
        url: String,
        md5: String,
        packageName: String,
        versionCode: Long,
        delay: Int,
        installListener: InstallListener
    ) {
        DownloadTools.startDownload(
            context = context,
            title = "",
            msg = "",
            url = url,
            header = emptyMap(),
            path = "",
            isFilePath = false,
            md5 = md5,
            sha256 = "",
            packageName = packageName,
            versionCode = versionCode,
            forceDownloadNew = false,
            useMobile = true,
            actionKey = DownloadFileUtils.DOWNLOAD_ACTION_KEY_APK,
            forceDownload = false,
            needRecord = false,
            downloadListener = SimpleAPKDownloadListener(
                context,
                packageName,
                installListener
            )
        )

    }
}