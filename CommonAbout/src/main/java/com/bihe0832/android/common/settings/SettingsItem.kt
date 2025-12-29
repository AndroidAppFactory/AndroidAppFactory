package com.bihe0832.android.common.settings

import android.app.Activity
import android.view.View
import com.bihe0832.android.common.settings.card.SettingsDataGo
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.framework.router.RouterAction
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.framework.router.openFeedback
import com.bihe0832.android.framework.router.openZixieWeb
import com.bihe0832.android.framework.router.shareAPP
import com.bihe0832.android.framework.update.UpdateDataFromCloud
import com.bihe0832.android.lib.superapp.QQHelper
import com.bihe0832.android.lib.superapp.WechatOfficialAccount
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.impl.LoadingDialog
import com.bihe0832.android.lib.ui.dialog.tools.DialogUtils
import com.bihe0832.android.lib.utils.apk.APKUtils
import com.bihe0832.android.lib.utils.intent.IntentUtils
import com.bihe0832.android.model.res.R as ModelResR
import com.bihe0832.android.lib.aaf.res.R as ResR
import com.bihe0832.android.framework.R as FrameworkR

/**
 *
 * @author zixie code@bihe0832.com Created on 8/3/21.
 *
 */
object SettingsItem {

    fun getVersionList(): SettingsDataGo {
        return getVersionList(ZixieContext.applicationContext!!.getString(ModelResR.string.settings_version_title))
    }

    fun getVersionList(title: String): SettingsDataGo {
        return SettingsDataGo(title).apply {
            mItemIconRes = ResR.drawable.icon_menu
            mShowDriver = true
            mShowGo = true
            mHeaderListener = View.OnClickListener {
                ThemeResourcesManager.getString(FrameworkR.string.version_url).let { url ->
                    if (url.isNullOrEmpty()) {
                        ZixieContext.showWaiting()
                    } else {
                        openZixieWeb(url)
                    }
                }
            }
        }
    }

    fun getUpdate(
        titile: String,
        cloud: UpdateDataFromCloud?,
        listener: View.OnClickListener
    ): SettingsDataGo {
        return SettingsDataGo(titile).apply {
            mItemIconRes = ResR.drawable.icon_update
            mHeaderTextBold = true
            mShowDriver = true
            mShowGo = true
            if (cloud?.canShowNew() == true) {
                mItemNewNum = 0
            }
            mHeaderListener = listener
        }
    }

    fun getAboutTitle(): String {
        return ThemeResourcesManager.getString(ModelResR.string.settings_about_title) + ThemeResourcesManager.getString(
            ModelResR.string.about_app
        )
    }

    fun getAboutAPP(cloud: UpdateDataFromCloud?, listener: View.OnClickListener): SettingsDataGo {
        return getUpdate(getAboutTitle(), cloud, listener).apply {
            mItemIconRes = ResR.drawable.icon_android
        }
    }

    fun getUpdate(cloud: UpdateDataFromCloud?, listener: View.OnClickListener): SettingsDataGo {
        return getUpdate(
            ThemeResourcesManager.getString(ModelResR.string.settings_update_title)
                ?: "",
            cloud,
            listener,
        ).apply {
            mItemIconRes = ResR.drawable.icon_update
        }
    }

    fun getMessage(msgNum: Int, listener: View.OnClickListener): SettingsDataGo {
        return SettingsDataGo(
            ThemeResourcesManager.getString(
                ModelResR.string.settings_message_title
            )
        ).apply {
            mItemIconRes = ResR.drawable.icon_message
            mShowDriver = true
            mShowGo = true
            mItemNewNum = msgNum
            mHeaderListener = listener
        }
    }

    fun getFeedbackURL(): SettingsDataGo {
        return getFeedbackURL(ThemeResourcesManager.getString(ModelResR.string.feedback))
    }

    fun getFeedbackURL(title: String?): SettingsDataGo {
        return SettingsDataGo(title).apply {
            mItemIconRes = FrameworkR.drawable.icon_feedback
            mShowDriver = true
            mShowGo = true
            mHeaderListener = View.OnClickListener {
                openFeedback()
            }
        }
    }

    fun getFeedbackMail(activity: Activity?): SettingsDataGo {
        return getFeedbackMail(activity, ThemeResourcesManager.getString(ModelResR.string.feedback))
    }

    fun getFeedbackMail(activity: Activity?, title: String?): SettingsDataGo {
        val mail = ThemeResourcesManager.getString(FrameworkR.string.feedback_mail)
        return SettingsDataGo(title).apply {
            mItemIconRes = ResR.drawable.icon_message
            mShowDriver = true
            mShowGo = true
            mTipsText = "<u>$mail</u>"
            mHeaderTipsListener = View.OnClickListener {
                IntentUtils.sendMail(
                    activity,
                    mail,
                    String.format(
                        ThemeResourcesManager.getString(ModelResR.string.feedback_mail_title)!!,
                        APKUtils.getAppName(activity),
                    ),
                    "",
                ).let { res ->
                    if (!res) {
                        openFeedback()
                    }
                }
            }
        }
    }

    fun getQQService(activity: Activity?): SettingsDataGo {
        return getQQService(activity, "客服QQ")
    }

    fun getQQService(activity: Activity?, title: String): SettingsDataGo {
        return SettingsDataGo(title).apply {
            val feedbackQQnumber = ThemeResourcesManager.getString(FrameworkR.string.feedback_qq)
            mItemIconRes = ResR.drawable.icon_qq
            mShowDriver = true
            mShowGo = true
            mTipsText = "<u>$feedbackQQnumber</u>"
            mHeaderTipsListener = View.OnClickListener {
                val res = QQHelper.openQQChat(activity, feedbackQQnumber)
                if (!res) {
                    ZixieContext.showToastJustAPPFront(ThemeResourcesManager.getString(ModelResR.string.contact_QQ_join_failed)!!)
                }
            }
        }
    }

    fun getDebug(): SettingsDataGo {
        return getDebug("调试")
    }

    fun getDebug(title: String): SettingsDataGo {
        return SettingsDataGo(title).apply {
            mItemIconRes = ResR.drawable.icon_android
            mShowDriver = true
            mShowGo = true
            mHeaderListener = View.OnClickListener {
                RouterAction.openPageByRouter(RouterConstants.MODULE_NAME_DEBUG)
            }
        }
    }

    fun getWechat(activity: Activity?): SettingsDataGo {
        return getWechat(
            activity,
            activity?.getString(ModelResR.string.wechat_official_account) ?: "",
            activity?.getString(ModelResR.string.wechat_sub_content_tips) ?: ""
        )
    }

    fun getWechat(activity: Activity?, title: String, tipsText: String): SettingsDataGo {
        return SettingsDataGo(title).apply {
            mItemIconRes = ResR.drawable.icon_wechat
            mShowDriver = true
            mShowGo = true
            mTipsText = "<u>$tipsText</u>"
            mHeaderTipsListener = View.OnClickListener {
                activity?.let {
                    WechatOfficialAccount.showSubscribe(
                        activity,
                        WechatOfficialAccount.WechatOfficialAccountData().apply {
                            this.mAccountID = ThemeResourcesManager.getString(ModelResR.string.wechat_id)
                            this.mAccountTitle =
                                ThemeResourcesManager.getString(ModelResR.string.wechat_name)
                            this.mSubContent =
                                ThemeResourcesManager.getString(ModelResR.string.wechat_sub_content)
                        },
                    )
                }
            }
        }
    }

    fun getZixie(): SettingsDataGo {
        return getZixie("关于开发者")
    }

    fun getZixie(title: String): SettingsDataGo {
        return SettingsDataGo(title).apply {
            mItemIconRes = ResR.drawable.icon_author
            mShowDriver = true
            mShowGo = true
            mHeaderListener = View.OnClickListener {
                openZixieWeb("file:///android_asset/web/author.html")
            }
        }
    }

    fun getShareAPP(canSendAPK: Boolean): SettingsDataGo {
        return getShareAPP(
            canSendAPK,
            ZixieContext.applicationContext!!.getString(ModelResR.string.com_bihe0832_share_title)
        )
    }

    fun getShareAPP(canSendAPK: Boolean, title: String): SettingsDataGo {
        return SettingsDataGo(title).apply {
            mItemIconRes = ResR.drawable.icon_share
            mShowDriver = true
            mShowGo = true
            mHeaderListener = View.OnClickListener {
                shareAPP(canSendAPK)
            }
        }
    }

    open fun clearCache(activity: Activity) {
        DialogUtils.showConfirmDialog(
            activity,
            ThemeResourcesManager.getString(ModelResR.string.settings_clear_tips) ?: "",
            true,
            object : OnDialogListener {
                override fun onPositiveClick() {
                    val loadingDialog = LoadingDialog(activity)
                    loadingDialog.setCanCanceled(false)
                    loadingDialog.setIsFullScreen(true)
                    loadingDialog.setHtmlTitle(
                        ThemeResourcesManager.getString(ModelResR.string.settings_clear_loading) ?: ""
                    )
                    loadingDialog.show()
                    ThreadManager.getInstance().start {
                        AAFFileWrapper.clear()
                        ThreadManager.getInstance().runOnUIThread { ZixieContext.exitAPP() }
                    }
                }

                override fun onNegativeClick() {
                }

                override fun onCancel() {
                    onNegativeClick()
                }
            },
        )
    }

    fun getClearCache(activity: Activity): SettingsDataGo {
        return getClearCache(activity, activity.getString(ModelResR.string.settings_clear_title))
    }

    fun getClearCache(activity: Activity, title: String): SettingsDataGo {
        return SettingsDataGo(title).apply {
            mItemIconRes = ResR.drawable.icon_delete_fill
            mShowDriver = true
            mShowGo = true
            mHeaderListener = View.OnClickListener {
                clearCache(activity)
            }
        }
    }
}
