/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.intent

import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import android.view.View
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.base.debug.lock.DebugLockService
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.common.praise.UserPraiseManager
import com.bihe0832.android.framework.privacy.AgreementPrivacy
import com.bihe0832.android.framework.router.RouterAction
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.ui.dialog.callback.DialogCompletedStringCallback
import com.bihe0832.android.lib.ui.dialog.tools.DialogUtils
import com.bihe0832.android.lib.utils.intent.IntentUtils

class DebugIntentFragment : DebugEnvFragment() {
    val LOG_TAG = this.javaClass.simpleName
    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getDebugItem("打开指定schema", View.OnClickListener { openSchema() }))

            add(
                getDebugItem(
                    "弹出隐私弹框页面",
                    View.OnClickListener {
                        AgreementPrivacy.showPrivacy(activity!!) {

                        }
                    },
                ),
            )

            add(
                getDebugItem(
                    "默认关于页",
                    View.OnClickListener { RouterHelper.openPageByRouter(RouterConstants.MODULE_NAME_BASE_ABOUT) },
                ),
            )

            add(
                getDebugItem(
                    "打开指定应用设置",
                    View.OnClickListener {
                        IntentUtils.startAppSettings(
                            context?.applicationContext,
                            "com.bihe0832.android.app.test",
                            "",
                            true,
                        )
                    },
                ),
            )

            add(
                getDebugItem(
                    "打开应用安装界面",
                    View.OnClickListener {
                        IntentUtils.startAppSettings(
                            context,
                            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES
                        )
                    },
                ),
            )

            add(
                getDebugItem(
                    "打开反馈页面",
                    View.OnClickListener {
                        RouterAction.openFinalURL(getFeedBackURL())
                    },
                ),
            )

            add(
                getDebugItem(
                    "启动锁屏页面",
                    View.OnClickListener {
                        DebugLockService.startLockServiceWithPermission(context)
                    },
                ),
            )

            add(
                getDebugItem(
                    "启动Service",
                    View.OnClickListener {
                        val intent = Intent()
                        intent.setComponent(
                            ComponentName(
                                context!!.applicationContext!!,
                                "com.bihe0832.android.base.debug.lock.DebugLockService",
                            ),
                        )
                        context!!.applicationContext!!.startService(intent)
                    },
                ),
            )

            add(
                getDebugItem(
                    "弹出评分页面",
                    View.OnClickListener {
                        UserPraiseManager.showUserPraiseDialog(activity!!, getFeedBackURL())
                    },
                ),
            )
        }
    }

    private fun openSchema() {
        DialogUtils.showInputDialog(
            context!!,
            "Schma调试",
            "zapk://about",
            object : DialogCompletedStringCallback {
                override fun onResult(p0: String?) {
                    IntentUtils.jumpToOtherApp(context, p0)
                }
            },
        )
    }

    private fun getFeedBackURL(): String {
        val map = HashMap<String, String>()
        map[RouterConstants.INTENT_EXTRA_KEY_WEB_URL] =
            URLUtils.encode("https://support.qq.com/embed/phone/290858/large/")
        return RouterAction.getFinalURL(RouterConstants.MODULE_NAME_FEEDBACK, map)
    }
}
