package com.bihe0832.android.framework.update

import android.app.Activity
import com.bihe0832.android.framework.R
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.wrapper.DownloadFile
import com.bihe0832.android.lib.download.wrapper.SimpleDownloadListener
import com.bihe0832.android.lib.install.InstallUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.CommonDialog
import com.bihe0832.android.lib.ui.dialog.OnDialogListener
import com.bihe0832.android.lib.ui.toast.ToastUtil
import com.bihe0832.android.lib.utils.intent.IntentUtils


object UpdateHelper {
    var hasShow = false

    fun showUpdate(activity: Activity, checkUpdateByUser: Boolean, info: UpdateDataFromCloud) {
        when (info.updateType) {
            //强更、弹框、必弹
            UpdateDataFromCloud.UPDATE_TYPE_MUST,
            UpdateDataFromCloud.UPDATE_TYPE_MUST_JUMP,
            UpdateDataFromCloud.UPDATE_TYPE_NEED,
            UpdateDataFromCloud.UPDATE_TYPE_NEED_JUMP -> {
                showUpdateDialog(activity, info.newVersionName, info.newVersionInfo, info.newVersionURL, info.newVersionMD5, info.updateType)
            }

            //红点、无状态，用户触发才弹
            UpdateDataFromCloud.UPDATE_TYPE_RED,
            UpdateDataFromCloud.UPDATE_TYPE_RED_JUMP,
            UpdateDataFromCloud.UPDATE_TYPE_HAS_NEW,
            UpdateDataFromCloud.UPDATE_TYPE_HAS_NEW_JUMP -> {
                if (checkUpdateByUser) {
                    showUpdateDialog(activity, info.newVersionName, info.newVersionInfo, info.newVersionURL, info.newVersionMD5, info.updateType)
                }
            }

            UpdateDataFromCloud.UPDATE_TYPE_NEW -> {
                if (checkUpdateByUser) {
                    ToastUtil.showLong(activity, "当前已是最新版本")
                }
            }
        }
    }

    private fun showUpdateDialog(activity: Activity, versionName: String, desc: String, url: String, md5: String, type: Int) {
        if (hasShow) {
            return
        }
        hasShow = true
        ThreadManager.getInstance().runOnUIThread {
            CommonDialog(activity).apply {
                title = activity.resources.getString(R.string.dialog_apk_update) + versionName
                setHtmlContent(activity.getString(R.string.dialog_apk_updateinfo) + "<BR>" + desc)
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

                            UpdateDataFromCloud.UPDATE_TYPE_HAS_NEW,
                            UpdateDataFromCloud.UPDATE_TYPE_RED,
                            UpdateDataFromCloud.UPDATE_TYPE_NEED -> {
                                ThreadManager.getInstance().run { startUpdate(activity, versionName, desc, url, md5, true) }
                                dismiss()
                                hasShow = false
                            }
                            UpdateDataFromCloud.UPDATE_TYPE_HAS_NEW_JUMP,
                            UpdateDataFromCloud.UPDATE_TYPE_RED_JUMP,
                            UpdateDataFromCloud.UPDATE_TYPE_NEED_JUMP -> {
                                IntentUtils.openWebPage(url, ZixieContext.applicationContext)
                                dismiss()
                                hasShow = false
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
                            UpdateDataFromCloud.UPDATE_TYPE_HAS_NEW,
                            UpdateDataFromCloud.UPDATE_TYPE_RED,
                            UpdateDataFromCloud.UPDATE_TYPE_NEED,
                            UpdateDataFromCloud.UPDATE_TYPE_HAS_NEW_JUMP,
                            UpdateDataFromCloud.UPDATE_TYPE_RED_JUMP,
                            UpdateDataFromCloud.UPDATE_TYPE_NEED_JUMP -> {
                                dismiss()
                                hasShow = false
                            }
                        }
                    }

                    override fun onCancel() {
                        onNegativeClick()
                    }
                })
                setShouldCanceled(false)
                setCancelable(type != UpdateDataFromCloud.UPDATE_TYPE_MUST)
            }.show()
        }
    }

    fun startUpdate(activity: Activity, version: String, versionInfo: String, url: String, md5: String, canCancel: Boolean) {
        val updateTitle = String.format(ZixieContext.applicationContext!!.getString(R.string.dialog_apk_updating), version)
        var dialogListenerWhenDownload = object : OnDialogListener {
            override fun onPositiveClick() {
                hasShow = false
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


        var downloadListener = object : SimpleDownloadListener() {
            override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                hasShow = false
                if (!canCancel) {
                    ThreadManager.getInstance().start({ ZixieContext.exitAPP() }, 3000L)
                }
            }

            override fun onComplete(filePath: String, item: DownloadItem) {
                ZLog.i("startDownloadApk download installApkPath: $filePath")
                ThreadManager.getInstance().start({
                    ThreadManager.getInstance().runOnUIThread {
                        CommonDialog(activity).apply {
                            title = updateTitle
                            content = versionInfo
                            positive = "点击安装"
                            negative = "稍候安装"
                            setOnClickBottomListener(object : OnDialogListener {
                                override fun onPositiveClick() {
                                    ThreadManager.getInstance().runOnUIThread {
                                        InstallUtils.installAPP(activity, filePath)
                                    }
                                }

                                override fun onNegativeClick() {
                                    if (!canCancel) {
                                        ThreadManager.getInstance().start({ ZixieContext.exitAPP() }, 300L)
                                    }else{
                                        dismiss()
                                    }
                                }

                                override fun onCancel() {
                                    onNegativeClick()
                                }
                            })
                        }.let { it.show() }
                        InstallUtils.installAPP(activity, filePath)
                    }
                }, 1)
            }

            override fun onProgress(item: DownloadItem) {

            }
        }
        DownloadFile.startDownloadWithProcess(
                activity,
                updateTitle,
                versionInfo,
                url, md5,
                canCancel, true,
                dialogListenerWhenDownload, downloadListener)
    }
}