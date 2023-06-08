/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.intent


import android.provider.Settings
import android.view.View
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.base.debug.lock.DebugLockService
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.common.praise.UserPraiseManager
import com.bihe0832.android.framework.router.RouterAction
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.utils.intent.IntentUtils


class DebugIntentFragment : DebugEnvFragment() {
    val LOG_TAG = this.javaClass.simpleName

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugItemData("默认关于页", View.OnClickListener { RouterHelper.openPageByRouter(RouterConstants.MODULE_NAME_BASE_ABOUT) }))

            add(DebugItemData("打开应用安装界面", View.OnClickListener {
                IntentUtils.startAppSettings(context, Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            }))

            add(DebugItemData("打开反馈页面", View.OnClickListener {
                RouterAction.openFinalURL(getFeedBackURL())
            }))

            add(DebugItemData("启动锁屏页面", View.OnClickListener {
                DebugLockService.startLockServiceWithPermission(context)
            }))

            add(DebugItemData("弹出评分页面", View.OnClickListener {
                UserPraiseManager.showUserPraiseDialog(activity!!, getFeedBackURL())

            }))
        }
    }

    private fun getFeedBackURL(): String {
        val map = HashMap<String, String>()
        map[RouterConstants.INTENT_EXTRA_KEY_WEB_URL] = URLUtils.encode("https://support.qq.com/embed/phone/290858/large/")
        return RouterAction.getFinalURL(RouterConstants.MODULE_NAME_FEEDBACK, map)
    }
}