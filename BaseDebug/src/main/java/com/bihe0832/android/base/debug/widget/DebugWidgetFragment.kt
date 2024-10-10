/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.widget

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Intent
import android.view.View
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.common.debug.widget.app.AAFDebugWidgetProviderDetail
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.constant.ZixieActivityRequestCode
import com.bihe0832.android.framework.router.RouterAction
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.permission.PermissionManager
import com.bihe0832.android.lib.permission.ui.PermissionDialog
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.lib.utils.intent.IntentUtils
import com.bihe0832.android.lib.widget.WidgetUpdateManager
import com.bihe0832.android.lib.widget.permission.ShortcutPermission
import com.bihe0832.android.lib.widget.tools.WidgetTools


class DebugWidgetFragment : DebugEnvFragment() {
    val LOG_TAG = this.javaClass.simpleName
    val mAppWidgetManager = AppWidgetManager.getInstance(ZixieContext.applicationContext)
    var permissionDialog: PermissionDialog? = null
    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugItemData("简单测试函数", View.OnClickListener { testFunc() }))
            add(
                DebugItemData(
                    "Activity使用系统选择并添加Widget到桌面",
                    View.OnClickListener {
                        WidgetTools.pickWidget(activity!!, ZixieActivityRequestCode.SELECT_WIDGET)
                    },
                ),
            )

            add(
                DebugItemData(
                    "Fragment使用系统选择并添加Widget到桌面",
                    View.OnClickListener {
                        WidgetTools.pickWidget(this@DebugWidgetFragment, ZixieActivityRequestCode.SELECT_WIDGET)
                    },
                ),
            )
            add(
                DebugItemData(
                    "使用AAF选择并添加Widget到桌面",
                    View.OnClickListener {
                        WidgetTools.pickWidget(context!!)
                    },
                ),
            )
            add(
                DebugItemData(
                    "刷新指定Widget信息",
                    View.OnClickListener {
                        WidgetUpdateManager.updateWidget(
                            context!!,
                            TestWorker1::class.java,
                            canAutoUpdateByOthers = false
                        )
                    },
                ),
            )

            add(
                DebugItemData(
                    "刷新所有Widget信息",
                    View.OnClickListener {
                        WidgetUpdateManager.updateAllWidgets(context!!)
                    },
                ),
            )

            add(
                DebugItemData(
                    "获取当前应用的所有Widget信息",
                    View.OnClickListener {
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
                    },
                ),
            )

            add(
                DebugItemData(
                    "添加Widget到桌面",
                    View.OnClickListener {
                        WidgetTools.addWidgetToHome(activity!!, AAFDebugWidgetProviderDetail::class.java)
                    },
                ),
            )
            add(
                DebugItemData(
                    "添加Widget到桌面(检查权限)",
                    View.OnClickListener {
                        addWidgetWithPermission()
                    },
                ),
            )


            add(
                DebugItemData(
                    "启动Service",
                    View.OnClickListener {
                        val intent = Intent()
                        intent.setAction("com.example.wecodeprocess.action.OPEN_WECODE_FROM_WECODE")
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
        }
    }

    private fun testFunc() {

    }


    private fun addWidgetWithPermission() {
        if (!WidgetTools.hasAddWidgetPermission(context!!)) {
            val scene = "widget"
            PermissionManager.addPermissionGroup(
                scene, Manifest.permission.INSTALL_SHORTCUT, mutableListOf(Manifest.permission.INSTALL_SHORTCUT)
            )
            PermissionManager.addPermissionGroupContent(
                scene,
                Manifest.permission.INSTALL_SHORTCUT,
                "检测到权限未开启，请前往应用设置，权限设置，其他权限，开启“创建桌面快捷方式”权限后即可添加。"
            )
            PermissionDialog(context!!).let {
                it.show(
                    scene, Manifest.permission.INSTALL_SHORTCUT, true, object : OnDialogListener {
                        override fun onPositiveClick() {
                            IntentUtils.startAppDetailSettings(context)
                            permissionDialog = it
                        }

                        override fun onNegativeClick() {
                            it.dismiss()
                            ZLog.d("failed")
                        }

                        override fun onCancel() {
                            it.dismiss()
                            ZLog.d("failed")
                        }
                    }
                )
            }
        } else {
            WidgetTools.addWidgetToHome(activity!!, AAFDebugWidgetProviderDetail::class.java)
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean, hasCreateView: Boolean) {
        super.setUserVisibleHint(isVisibleToUser, hasCreateView)
        if (isVisibleToUser && hasCreateView && permissionDialog != null) {
            if (ShortcutPermission.hasPermission(context)) {
                permissionDialog?.dismiss()
                WidgetTools.addWidgetToHome(activity!!, AAFDebugWidgetProviderDetail::class.java)
            }
            permissionDialog = null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == ZixieActivityRequestCode.SELECT_WIDGET) {
            val appWidgetId = WidgetTools.getAppWidgetId(data)
            WidgetTools.addWidgetToHome(context!!, appWidgetId)
        }
    }

    private fun getFeedBackURL(): String {
        val map = HashMap<String, String>()
        map[RouterConstants.INTENT_EXTRA_KEY_WEB_URL] =
            URLUtils.encode("https://support.qq.com/embed/phone/290858/large/")
        return RouterAction.getFinalURL(RouterConstants.MODULE_NAME_FEEDBACK, map)
    }
}
