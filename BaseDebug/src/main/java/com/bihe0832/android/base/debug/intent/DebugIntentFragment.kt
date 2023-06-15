/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.intent


import android.app.Activity
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import android.view.View
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.base.debug.lock.DebugLockService
import com.bihe0832.android.base.debug.widget.TestWorker1
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.common.debug.widget.app.AAFDebugWidgetProviderDetail
import com.bihe0832.android.common.praise.UserPraiseManager
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.constant.ZixieActivityRequestCode
import com.bihe0832.android.framework.router.RouterAction
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.ui.dialog.OnDialogListener
import com.bihe0832.android.lib.utils.intent.IntentUtils
import com.bihe0832.android.lib.widget.WidgetUpdateManager
import com.bihe0832.android.lib.widget.tools.WidgetSelectDialog
import com.bihe0832.android.lib.widget.tools.WidgetTools


class DebugIntentFragment : DebugEnvFragment() {
    val LOG_TAG = this.javaClass.simpleName
    val mAppWidgetManager = AppWidgetManager.getInstance(ZixieContext.applicationContext);
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

            add(DebugItemData("刷新指定Widget信息", View.OnClickListener {
                WidgetUpdateManager.updateWidget(context!!, TestWorker1::class.java, canAutoUpdateByOthers = false, updateAll = false)

            }))

            add(DebugItemData("刷新所有Widget信息", View.OnClickListener {
                WidgetUpdateManager.updateAllWidgets(context!!)
            }))

            add(DebugItemData("获取当前应用的所有Widget信息", View.OnClickListener {
                val widgetProviders: List<AppWidgetProviderInfo> = mAppWidgetManager.getInstalledProviders()
                for (widgetProvider in widgetProviders) {
                    if (widgetProvider.provider.packageName.equals(context!!.packageName)) {
                        val widgetId = widgetProvider.provider.shortClassName.hashCode()
                        val label: CharSequence = widgetProvider.loadLabel(context!!.packageManager)
                        ZLog.d("Widget:$widgetProvider")
                        ZLog.d("widgetId:$widgetId")
                        ZLog.d("label:$label")
                    }
                }
            }))

            add(DebugItemData("选择并添加Widget到桌面", View.OnClickListener {
//                WidgetTools.pickWidget(activity!!, ZixieActivityRequestCode.SELECT_WIDGET)
                WidgetTools.pickWidget(context!!)
            }))

            add(DebugItemData("添加Widget到桌面", View.OnClickListener {
                WidgetTools.addWidgetToHome(activity!!, AAFDebugWidgetProviderDetail::class.java)
            }))

            add(DebugItemData("启动Service", View.OnClickListener {
                val intent = Intent();
                intent.setAction("com.example.wecodeprocess.action.OPEN_WECODE_FROM_WECODE");
                intent.setComponent(ComponentName(context!!.applicationContext!!, "com.bihe0832.android.base.debug.lock.DebugLockService"))
                context!!.applicationContext!!.startService(intent)
            }))


            add(DebugItemData("弹出评分页面", View.OnClickListener {
                UserPraiseManager.showUserPraiseDialog(activity!!, getFeedBackURL())

            }))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == ZixieActivityRequestCode.SELECT_WIDGET) {
            WidgetTools.onActivityResult(context!!, data)
        }

    }

    private fun getFeedBackURL(): String {
        val map = HashMap<String, String>()
        map[RouterConstants.INTENT_EXTRA_KEY_WEB_URL] = URLUtils.encode("https://support.qq.com/embed/phone/290858/large/")
        return RouterAction.getFinalURL(RouterConstants.MODULE_NAME_FEEDBACK, map)
    }
}