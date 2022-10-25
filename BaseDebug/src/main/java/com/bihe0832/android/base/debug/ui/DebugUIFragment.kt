/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.ui


import android.provider.Settings
import android.view.View
import android.widget.Toast
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.base.debug.touch.TouchRegionActivity
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.common.praise.UserPraiseManager
import com.bihe0832.android.framework.router.RouterAction
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.debug.DebugTools
import com.bihe0832.android.lib.lifecycle.ActivityObserver
import com.bihe0832.android.lib.lifecycle.ApplicationObserver
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.ui.dialog.input.InputDialogCompletedCallback
import com.bihe0832.android.lib.ui.toast.ToastUtil
import com.bihe0832.android.lib.utils.intent.IntentUtils


class DebugUIFragment : DebugEnvFragment() {
    val LOG_TAG = this.javaClass.simpleName

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(
                    DebugItemData(
                            "默认关于页",
                            View.OnClickListener { RouterHelper.openPageByRouter(RouterConstants.MODULE_NAME_BASE_ABOUT) })
            )

            add(DebugItemData("打开应用安装界面", View.OnClickListener {
                IntentUtils.startAppSettings(
                        context,
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES
                )
            }))
            add(DebugItemData("TextView对HTML的支持测试", View.OnClickListener {
                showInputDialog("TextView对HTML的支持测试",
                        "请在输入框输入需要验证的文本内容，无需特殊编码",
                        "<font color='#428bca'>测试文字加粗</font> <BR> 正常的文字效果<BR> <b>测试文字加粗</b> <em>文字斜体</em> <p><font color='#428bca'>修改文字颜色</font></p>",
                        object : InputDialogCompletedCallback {
                            override fun onInputCompleted(result: String?) {
                                DebugTools.showInfoWithHTML(
                                        context,
                                        "TextView对HTML的支持测试",
                                        result,
                                        "分享给我们"
                                )
                            }

                        })
            }))

            add(DebugItemData("点击区扩大Demo", View.OnClickListener {
                startActivityWithException(TouchRegionActivity::class.java)
            }))

            add(DebugItemData("Toast测试", View.OnClickListener {
                ToastUtil.showTop(
                        context,
                        "这是一个测试用的<font color ='#38ADFF'><b>测试消息</b></font>",
                        Toast.LENGTH_LONG
                )
            }))

            add(DebugItemData("应用前后台信息", View.OnClickListener { testAPPObserver() }))

            add(DebugItemData("打开反馈页面", View.OnClickListener {
                val map = HashMap<String, String>()
                map[com.bihe0832.android.framework.router.RouterConstants.INTENT_EXTRA_KEY_WEB_URL] = URLUtils.encode("https://support.qq.com/embed/phone/290858/large/")
                RouterAction.openPageByRouter(com.bihe0832.android.framework.router.RouterConstants.MODULE_NAME_FEEDBACK, map)
            }))

            add(DebugItemData("弹出评分页面", View.OnClickListener {
                UserPraiseManager.showUserPraiseDialog(activity!!, RouterAction.getFinalURL(com.bihe0832.android.framework.router.RouterConstants.MODULE_NAME_FEEDBACK))
            }))
        }
    }

    private fun testAPPObserver() {
        ZLog.d("testAPPObserver", "getAPPStartTime ： ${ApplicationObserver.getAPPStartTime()}")
        ZLog.d("testAPPObserver", "getLastPauseTime ： ${ApplicationObserver.getLastPauseTime()}")
        ZLog.d(
                "testAPPObserver",
                "getLastResumedTime ： ${ApplicationObserver.getLastResumedTime()}"
        )
        ZLog.d("testAPPObserver", "getCurrentActivity ： ${ActivityObserver.getCurrentActivity()}")
        ActivityObserver.getActivityList().forEach {
            ZLog.d("testAPPObserver", "getCurrentActivity ： ${it.javaClass.name} - ${it.hashCode()} - ${it.taskId}")
        }

    }
}