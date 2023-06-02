package com.bihe0832.android.common.settings

import android.app.Activity
import android.content.Context
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

    fun getUpdate(activity: Activity?, cloud: UpdateDataFromCloud?, listener: View.OnClickListener): SettingsData {
        return SettingsData(ThemeResourcesManager.getString(R.string.settings_update_title)).apply {
            mItemIconRes = R.drawable.icon_update
            mHeaderTextBold = true
            mShowDriver = true
            mShowGo = true
            mItemIsNew = cloud?.canShowNew() ?: false
            mHeaderListener = listener
        }
    }

    fun getAboutTitle(context: Context): String {
        return "关于" + ThemeResourcesManager.getString(R.string.app_name)
    }

    fun getAboutAPP(context: Context, cloud: UpdateDataFromCloud?, listener: View.OnClickListener): SettingsData {
        return SettingsData(getAboutTitle(context)).apply {
            mItemIconRes = R.drawable.icon_android
            mHeaderTextBold = true
            mShowDriver = true
            mShowGo = true
            mItemIsNew = cloud?.canShowNew() ?: false
            mHeaderListener = listener
        }
    }

    fun getMessage(msgNum: Int, listener: View.OnClickListener): SettingsData {
        return SettingsData("消息中心").apply {
            mItemIconRes = R.drawable.icon_message
            mShowDriver = true
            mShowGo = true
            mItemIsNew = msgNum > 0
            mHeaderListener = listener
        }
    }

    fun getFeedback(): SettingsData {
        return SettingsData("建议反馈").apply {
            mItemIconRes = R.drawable.icon_feedback
            mShowDriver = true
            mShowGo = true
            mHeaderListener = View.OnClickListener {
                openFeedback()
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

    fun getShareAPP(): SettingsData {
        return SettingsData("分享给好友").apply {
            mItemIconRes = R.drawable.icon_share
            mShowDriver = true
            mShowGo = true
            mHeaderListener = View.OnClickListener {
                shareAPP(true)
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