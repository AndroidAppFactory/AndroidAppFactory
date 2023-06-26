package com.bihe0832.android.common.settings

import android.app.Activity
import android.view.View
import com.bihe0832.android.common.about.R
import com.bihe0832.android.common.permission.PermissionFragment
import com.bihe0832.android.common.settings.card.SettingsData
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.router.*
import com.bihe0832.android.framework.ui.main.CommonRootActivity
import com.bihe0832.android.framework.update.UpdateDataFromCloud
import com.bihe0832.android.lib.superapp.QQHelper
import com.bihe0832.android.lib.superapp.WechatOfficialAccount
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.utils.apk.APKUtils
import com.bihe0832.android.lib.utils.intent.IntentUtils

/**
 *
 * @author zixie code@bihe0832.com Created on 8/3/21.
 *
 */
object SettingsItem {

    fun getVersionList(): SettingsData {
        return SettingsData("版本介绍").apply {
            mItemIconRes = R.drawable.icon_menu
            mShowDriver = true
            mShowGo = true
            mHeaderListener = View.OnClickListener {
                ThemeResourcesManager.getString(R.string.version_url).let { url ->
                    if (url.isNullOrEmpty()) {
                        ZixieContext.showWaiting()
                    } else {
                        openZixieWeb(url)
                    }
                }
            }
        }
    }

    fun getUpdate(titile: String, cloud: UpdateDataFromCloud?, listener: View.OnClickListener): SettingsData {
        return SettingsData(titile).apply {
            mItemIconRes = R.drawable.icon_update
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
        return "关于" + ThemeResourcesManager.getString(R.string.about_app)
    }

    fun getAboutAPP(cloud: UpdateDataFromCloud?, listener: View.OnClickListener): SettingsData {
        return getUpdate(getAboutTitle(), cloud, listener).apply {
            mItemIconRes = R.drawable.icon_android
        }
    }

    fun getUpdate(cloud: UpdateDataFromCloud?, listener: View.OnClickListener): SettingsData {
        return getUpdate(ThemeResourcesManager.getString(R.string.settings_update_title)
                ?: "", cloud, listener).apply {
            mItemIconRes = R.drawable.icon_update
        }
    }

    fun getMessage(msgNum: Int, listener: View.OnClickListener): SettingsData {
        return SettingsData(ThemeResourcesManager.getString(R.string.settings_message_title)).apply {
            mItemIconRes = R.drawable.icon_message
            mShowDriver = true
            mShowGo = true
            mItemNewNum = msgNum
            mHeaderListener = listener
        }
    }

    fun getFeedbackURL(): SettingsData {
        return getFeedbackURL(ThemeResourcesManager.getString(R.string.feedback))
    }

    fun getFeedbackURL(title: String?): SettingsData {
        return SettingsData(title).apply {
            mItemIconRes = R.drawable.icon_feedback
            mShowDriver = true
            mShowGo = true
            mHeaderListener = View.OnClickListener {
                openFeedback()
            }
        }
    }

    fun getFeedbackMail(activity: Activity?): SettingsData {
        return getFeedbackMail(activity, ThemeResourcesManager.getString(R.string.feedback))
    }

    fun getFeedbackMail(activity: Activity?, title: String?): SettingsData {
        val mail = ThemeResourcesManager.getString(R.string.feedback_mail)
        return SettingsData(title).apply {
            mItemIconRes = R.drawable.icon_message
            mShowDriver = true
            mShowGo = true
            mTipsText = "<u>${mail}</u>"
            mHeaderTipsListener = View.OnClickListener {
                IntentUtils.sendMail(activity, mail, String.format(ThemeResourcesManager.getString(R.string.feedback_mail_title)!!, APKUtils.getAppName(activity)), "").let { res ->
                    if (!res) {
                        openFeedback()
                    }
                }
            }
        }
    }

    fun getQQService(activity: Activity?): SettingsData {
        return SettingsData("客服QQ").apply {
            var feedbackQQnumber = ThemeResourcesManager.getString(R.string.feedback_qq)
            mItemIconRes = R.mipmap.ic_qq_black
            mShowDriver = true
            mShowGo = true
            mTipsText = "<u>${feedbackQQnumber}</u>"
            mHeaderTipsListener = View.OnClickListener {
                var res = QQHelper.openQQChat(activity, feedbackQQnumber)
                if (!res) {
                    ZixieContext.showToastJustAPPFront(ThemeResourcesManager.getString(R.string.contact_QQ_join_failed)!!)
                }
            }
        }
    }

    fun getDebug(): SettingsData {
        return SettingsData("调试").apply {
            mItemIconRes = R.drawable.icon_android
            mShowDriver = true
            mShowGo = true
            mHeaderListener = View.OnClickListener {
                RouterAction.openPageByRouter(RouterConstants.MODULE_NAME_DEBUG)
            }
        }
    }

    fun getWechat(activity: Activity?): SettingsData {
        return SettingsData("微信公众号").apply {
            mItemIconRes = R.mipmap.ic_wechat_black
            mShowDriver = true
            mShowGo = true
            mTipsText = "<u>前往关注</u>"
            mHeaderTipsListener = View.OnClickListener {
                activity?.let {
                    WechatOfficialAccount.showSubscribe(activity, WechatOfficialAccount.WechatOfficialAccountData().apply {
                        this.mAccountID = ThemeResourcesManager.getString(R.string.wechat_id)
                        this.mAccountTitle = ThemeResourcesManager.getString(R.string.wechat_name)
                        this.mSubContent = ThemeResourcesManager.getString(R.string.wechat_sub_content)
                    })
                }
            }
        }
    }

    fun getZixie(): SettingsData {
        return SettingsData("关于开发者").apply {
            mItemIconRes = R.drawable.icon_author
            mShowDriver = true
            mShowGo = true
            mHeaderListener = View.OnClickListener {
                openZixieWeb("file:///android_asset/web/author.html")
            }
        }
    }

    fun getShareAPP(canSendAPK: Boolean): SettingsData {
        return SettingsData("分享给好友").apply {
            mItemIconRes = R.drawable.icon_share
            mShowDriver = true
            mShowGo = true
            mHeaderListener = View.OnClickListener {
                shareAPP(canSendAPK)
            }
        }
    }

    fun getPermission(cls: Class<out PermissionFragment>): SettingsData {
        val title = "隐私及权限设置"
        return SettingsData(title).apply {
            mItemIconRes = R.drawable.icon_privacy_tip
            mShowDriver = true
            mShowGo = true
            mHeaderListener = View.OnClickListener {
                CommonRootActivity.startCommonRootActivity(it.context, cls, title)
            }
        }
    }
}