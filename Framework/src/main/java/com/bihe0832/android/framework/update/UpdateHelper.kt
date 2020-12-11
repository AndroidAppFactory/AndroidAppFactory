package com.bihe0832.android.framework.update

import android.app.Activity
import com.bihe0832.android.framework.R
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.CommonDialog
import com.bihe0832.android.lib.ui.dialog.OnDialogListener
import com.bihe0832.android.lib.ui.toast.ToastUtil
import com.bihe0832.android.lib.utils.intent.IntentUtils
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.download.DownloadHelper


object UpdateHelper {
    var hasShow =  false
    fun startUpdate(activity: Activity?, version: String, versionInfo: String?, url: String?, md5: String?, canCancle: Boolean) {

        DownloadHelper.startDownloadAPK(activity,
                String.format(ZixieContext.applicationContext!!.getString(R.string.dialog_apk_updating), version),
                versionInfo,
                url,
                md5,
                canCancle,
                object : OnDialogListener {
                    override fun onPositiveClick() {
                        if (!canCancle) {
                            ThreadManager.getInstance().start({ ZixieContext.exitAPP() }, 1)
                        }
                        hasShow = false
                    }

                    override fun onNegativeClick() {
                        if (!canCancle) {
                            ZixieContext.exitAPP()
                        }
                        hasShow = false
                    }

                    override fun onCancel() {
                        if (!canCancle) {
                            ThreadManager.getInstance().start({ ZixieContext.exitAPP() }, 1)
                        }
                        hasShow = false
                    }
                }, null
        )
    }

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
        if(hasShow){
            return
        }
        hasShow = true
        ThreadManager.getInstance().runOnUIThread {
            CommonDialog(activity).apply {
                title = activity.resources.getString(R.string.dialog_apk_update) + versionName
                setHtmlContent(activity.getString(R.string.dialog_apk_updateinfo) + "<BR>" + desc)
                positive = "现在更新"
                negative = "稍后更新"
                setOnClickBottomListener(object: OnDialogListener {
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

}