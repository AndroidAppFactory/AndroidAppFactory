package com.bihe0832.android.framework.update

import android.app.Activity
import android.text.TextUtils
import com.bihe0832.android.framework.R
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.router.openFeedback
import com.bihe0832.android.lib.download.DownloadErrorCode
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.wrapper.DownloadAPK
import com.bihe0832.android.lib.download.wrapper.SimpleInstallListener
import com.bihe0832.android.lib.install.InstallUtils
import com.bihe0832.android.lib.lifecycle.INSTALL_TYPE_NOT_FIRST
import com.bihe0832.android.lib.lifecycle.LifecycleHelper
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.CommonDialog
import com.bihe0832.android.lib.ui.dialog.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.impl.DialogUtils
import com.bihe0832.android.lib.ui.toast.ToastUtil
import com.bihe0832.android.lib.utils.intent.IntentUtils


object UpdateHelper {

    private const val TAG = "Update"
    var hasShow = false

    private fun startUpdate(activity: Activity, version: String, desc: String, url: String, md5: String, canCancel: Boolean) {

        var title = String.format(ThemeResourcesManager.getString(R.string.dialog_apk_updating)!!, "$version")

        var dialogListenerWhenDownload = object : OnDialogListener {
            override fun onPositiveClick() {
                if (!canCancel) {
                    ThreadManager.getInstance().start({ ZixieContext.exitAPP() }, 300L)
                }
            }

            override fun onNegativeClick() {
                onPositiveClick()
            }

            override fun onCancel() {
                onPositiveClick()
            }
        }

        var downloadListener = object : SimpleInstallListener(activity, activity.packageName, dialogListenerWhenDownload) {

            override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                if (errorCode in listOf(DownloadErrorCode.ERR_BAD_URL, DownloadErrorCode.ERR_HTTP_LENGTH_FAILED, DownloadErrorCode.ERR_MD5_BAD)) {
                    DialogUtils.showConfirmDialog(
                            activity, title,
                            ThemeResourcesManager.getString(R.string.dialog_apk_update_failed_desc)!!,
                            ThemeResourcesManager.getString(R.string.dialog_apk_update_failed_positive),
                            ThemeResourcesManager.getString(R.string.dialog_apk_update_failed_negative), object : OnDialogListener {
                        override fun onPositiveClick() {
                            if (canCancel) {
                                openFeedback()
                            } else {
                                IntentUtils.openWebPage(ThemeResourcesManager.getString(R.string.feedback_url), activity)
                            }
                            onNegativeClick()
                        }

                        override fun onNegativeClick() {
                            dialogListenerWhenDownload.onNegativeClick()
                        }

                        override fun onCancel() {
                            onNegativeClick()
                        }
                    })
                } else {
                    dialogListenerWhenDownload.onNegativeClick()
                }
            }
        }
        DownloadAPK.startDownloadWithProcess(activity, title, desc, url, md5, canCancel, true, dialogListenerWhenDownload, downloadListener)
    }

    private fun showUpdateDialogWithTitle(activity: Activity, versionName: String, titleString: String, desc: String, url: String, md5: String, type: Int) {
        if (hasShow) {
            return
        }
        hasShow = true
        ThreadManager.getInstance().runOnUIThread {
            CommonDialog(activity).apply {
                title = titleString
                setHtmlContent(desc)
                positive = "现在更新"
                negative = "稍后更新"
                setOnClickBottomListener(object : OnDialogListener {
                    override fun onPositiveClick() {
                        when (type) {
                            UpdateDataFromCloud.UPDATE_TYPE_MUST -> {
                                ThreadManager.getInstance().run { startUpdate(activity, versionName, desc, url, md5, false) }
                            }
                            UpdateDataFromCloud.UPDATE_TYPE_MUST_JUMP -> {
                                IntentUtils.openWebPage(url, ZixieContext.applicationContext)
                            }

                            UpdateDataFromCloud.UPDATE_TYPE_HAS_NEW, UpdateDataFromCloud.UPDATE_TYPE_RED, UpdateDataFromCloud.UPDATE_TYPE_NEED -> {
                                ThreadManager.getInstance().run { startUpdate(activity, versionName, desc, url, md5, true) }
                                if (InstallUtils.hasInstallAPPPermission(context, false, false)) {
                                    dismiss()
                                }
                            }
                            UpdateDataFromCloud.UPDATE_TYPE_HAS_NEW_JUMP, UpdateDataFromCloud.UPDATE_TYPE_RED_JUMP, UpdateDataFromCloud.UPDATE_TYPE_NEED_JUMP -> {
                                IntentUtils.openWebPage(url, ZixieContext.applicationContext)
                                dismiss()
                            }
                        }
                    }

                    override fun onNegativeClick() {
                        when (type) {
                            UpdateDataFromCloud.UPDATE_TYPE_MUST -> {
                                ZixieContext.exitAPP()
                            }
                            UpdateDataFromCloud.UPDATE_TYPE_MUST_JUMP -> {
                                ZixieContext.exitAPP()
                            }
                            UpdateDataFromCloud.UPDATE_TYPE_HAS_NEW, UpdateDataFromCloud.UPDATE_TYPE_RED, UpdateDataFromCloud.UPDATE_TYPE_NEED, UpdateDataFromCloud.UPDATE_TYPE_HAS_NEW_JUMP, UpdateDataFromCloud.UPDATE_TYPE_RED_JUMP, UpdateDataFromCloud.UPDATE_TYPE_NEED_JUMP -> {
                                dismiss()
                            }
                        }
                    }

                    override fun onCancel() {
                        onNegativeClick()
                    }
                })
                setShouldCanceled(type < UpdateDataFromCloud.UPDATE_TYPE_MUST_JUMP)
            }.show()
        }
    }

    fun showUpdateDialog(activity: Activity, versionName: String, titleString: String, desc: String, url: String, md5: String, type: Int) {
        hasShow = false
        var title = if (TextUtils.isEmpty(titleString)) {
            ThemeResourcesManager.getString(R.string.dialog_apk_update_title)+ versionName
        } else {
            titleString
        }
        showUpdateDialogWithTitle(activity, versionName, title, ThemeResourcesManager.getString(R.string.dialog_apk_update_info_pre) + desc, url, md5, type)
    }

    fun showUpdate(activity: Activity, checkUpdateByUser: Boolean, showIfNeedUpdate: Boolean, info: UpdateDataFromCloud) {
        when (info.updateType) {
            //强更、弹框、必弹
            UpdateDataFromCloud.UPDATE_TYPE_MUST, UpdateDataFromCloud.UPDATE_TYPE_MUST_JUMP -> {
                showUpdateDialog(activity, info.newVersionName, info.newVersionTitle, info.newVersionInfo, info.newVersionURL, info.newVersionMD5, info.updateType)
            }
            UpdateDataFromCloud.UPDATE_TYPE_NEED, UpdateDataFromCloud.UPDATE_TYPE_NEED_JUMP -> {
                if (checkUpdateByUser) {
                    showUpdateDialog(activity, info.newVersionName, info.newVersionTitle, info.newVersionInfo, info.newVersionURL, info.newVersionMD5, info.updateType)
                } else {
                    when {
                        LifecycleHelper.isFirstStart > INSTALL_TYPE_NOT_FIRST -> {
                            ZLog.e(TAG, "skip update by first update")
                        }
                        hasShow -> {
                            ZLog.e(TAG, "skip update by has show")
                        }
                        showIfNeedUpdate -> {
                            showUpdateDialog(activity, info.newVersionName, info.newVersionTitle, info.newVersionInfo, info.newVersionURL, info.newVersionMD5, info.updateType)
                        }
                        else -> {
                            ZLog.e(TAG, "skip update by showIfNeedUpdate is false")
                        }
                    }
                }
            }

            //红点、无状态，用户触发才弹
            UpdateDataFromCloud.UPDATE_TYPE_RED, UpdateDataFromCloud.UPDATE_TYPE_RED_JUMP, UpdateDataFromCloud.UPDATE_TYPE_HAS_NEW, UpdateDataFromCloud.UPDATE_TYPE_HAS_NEW_JUMP -> {
                if (checkUpdateByUser) {
                    showUpdateDialog(activity, info.newVersionName, info.newVersionTitle, info.newVersionInfo, info.newVersionURL, info.newVersionMD5, info.updateType)
                }
            }

            UpdateDataFromCloud.UPDATE_TYPE_NEW -> {
                if (checkUpdateByUser) {
                    ToastUtil.showLong(activity, "当前已是最新版本")
                }
            }
        }
    }

    fun showUpdate(activity: Activity, versionName: String, title: String, desc: String, url: String, md5: String, type: Int) {
        showUpdateDialog(activity, versionName, title, desc, url, md5, type)
    }
}