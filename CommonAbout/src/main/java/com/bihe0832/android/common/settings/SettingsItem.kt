package com.bihe0832.android.common.settings

import android.app.Activity
import android.view.View
import com.bihe0832.android.common.about.R
import com.bihe0832.android.common.about.card.SettingsData
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.router.RouterAction
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.framework.router.openZixieWeb
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.superapp.QQHelper
import com.bihe0832.android.lib.superapp.WechatOfficialAccount
import java.util.*

/**
 *
 * @author hardyshi code@bihe0832.com Created on 8/3/21.
 *
 */
object SettingsItem {

    fun getVersionList(): SettingsData {
        return SettingsData("版本介绍").apply {
            mItemIconRes = R.mipmap.icon_help
            mHeaderTextBold = true
            mShowDriver = true
            mShowGo = true
            mHeaderListener = View.OnClickListener {
                ZixieContext.applicationContext!!.resources.getString(R.string.version_url).let { url ->
                    if (url.isNullOrEmpty()) {
                        ZixieContext.showWaiting()
                    } else {
                        openZixieWeb(url)
                    }
                }
            }
        }
    }


    fun getUpdate(activity: Activity?, listener: View.OnClickListener): SettingsData {
        return SettingsData("版本更新").apply {
            mItemIconRes = R.mipmap.icon_update
            mHeaderTextBold = true
            mShowDriver = true
            mShowGo = true
            mHeaderListener = listener
        }
    }


    fun getFeedback(): SettingsData {
        return SettingsData("建议反馈").apply {
            mItemIconRes = R.mipmap.icon_feedback
            mShowDriver = true
            mShowGo = true
            mHeaderListener = View.OnClickListener {
                val map = HashMap<String, String>()
                map[RouterConstants.INTENT_EXTRA_KEY_WEB_URL] = URLUtils.encode(ZixieContext.applicationContext!!.resources.getString(R.string.feedback_url))
                RouterAction.openPageByRouter(RouterConstants.MODULE_NAME_FEEDBACK, map)
            }
        }
    }

    fun getQQService(activity: Activity?): SettingsData {
        return SettingsData("客服QQ").apply {
            var feedbackQQnumber = ZixieContext.applicationContext!!.resources.getString(R.string.feedback_qq)
            mItemIconRes = R.mipmap.icon_qq_black
            mShowDriver = true
            mShowGo = true
            mTipsText = "<u>${feedbackQQnumber}</u>"
            mHeaderTipsListener = View.OnClickListener {
                var res = QQHelper.openQQChat(activity, feedbackQQnumber)
                if (!res) {
                    ZixieContext.showToastJustAPPFront(ZixieContext.applicationContext!!.resources.getString(R.string.contact_QQ_join_failed))
                }
            }
        }
    }

    fun getDebug(): SettingsData {
        return SettingsData("调试").apply {
            mItemIconRes = R.mipmap.icon_author
            mShowDriver = true
            mShowGo = true
            mHeaderListener = View.OnClickListener {
                RouterAction.openPageByRouter(RouterConstants.MODULE_NAME_DEBUG)
            }
        }
    }

    fun getWechat(activity: Activity?): SettingsData {
        return SettingsData("微信公众号").apply {
            mItemIconRes = R.mipmap.icon_wechat_black
            mShowDriver = true
            mShowGo = true
            mTipsText = "<u>前往关注</u>"
            mHeaderTipsListener = View.OnClickListener {
                activity?.let {
                    WechatOfficialAccount.showSubscribe(activity, WechatOfficialAccount.WechatOfficialAccountData().apply {
                        this.mAccountID = ZixieContext.applicationContext!!.resources.getString(R.string.wechat_id)
                        this.mAccountTitle = ZixieContext.applicationContext!!.resources.getString(R.string.wechat_name)
                        this.mSubContent = ZixieContext.applicationContext!!.resources.getString(R.string.wechat_sub_content)
                    })
                }
            }
        }
    }

    fun getZixie(): SettingsData {
        return SettingsData("关于开发者").apply {
            mItemIconRes = R.mipmap.icon_author
            mShowDriver = true
            mShowGo = true
            mHeaderListener = View.OnClickListener {
                openZixieWeb("file:///android_asset/web/author.html")
            }
        }
    }
}