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

/**
 * 简化的安装监听器
 *
 * 继承自 SimpleDownloadListener，专门用于 APK 下载完成后的自动安装场景。
 * 集成了下载和安装的完整流程，提供友好的用户交互。
 *
 * 核心功能：
 * - 下载完成后自动触发安装
 * - 安装过程中显示加载对话框
 * - 支持解压、准备、安装等各个阶段的提示
 * - 安装失败/超时的友好提示
 * - 支持后台安装（用户可取消对话框）
 *
 * 安装流程：
 * 1. onComplete: 下载完成，检查文件类型
 * 2. onUnCompress: 如果是压缩包，显示解压提示
 * 3. onInstallPrepare: 显示安装准备提示
 * 4. onInstallStart: 开始安装，关闭加载对话框
 * 5. onInstallSuccess/onInstallFailed: 显示安装结果
 *
 * 参数说明：
 * @param activity 用于显示对话框和启动安装的 Activity
 * @param packageName 要安装的应用包名（用于安装验证）
 * @param listener 下载对话框的回调监听器（可选）
 *
 * 使用场景：
 * 1. 应用内更新：下载新版本并自动安装
 * 2. 插件安装：下载插件 APK 并安装
 * 3. 批量应用安装：下载多个 APK 并依次安装
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-01-10.
 * Description: 简化的安装监听器，封装下载完成后的自动安装流程
 */
open class SimpleInstallListener(
    private val activity: Activity,
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
                InstallUtils.installAPP(activity, filePath, "", installListener)
            }
        }
        return filePath
    }

    override fun onProgress(item: DownloadItem) {
    }
}
