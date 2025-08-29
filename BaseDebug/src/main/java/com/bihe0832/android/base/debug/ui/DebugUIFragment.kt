/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.ui


import android.view.View
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.base.debug.theme.ThemeActivity
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.item.getTipsItem
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.constant.Constants
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.app.icon.APPIconManager
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.language.MultiLanguageHelper
import com.bihe0832.android.lib.lifecycle.ActivityObserver
import com.bihe0832.android.lib.lifecycle.ApplicationObserver
import com.bihe0832.android.lib.log.ZLog
import java.util.Locale

class DebugUIFragment : DebugEnvFragment() {
    val LOG_TAG = this.javaClass.simpleName

    val list = listOf(
        "com.bihe0832.android.test.DefaultAlias",
        "com.bihe0832.android.test.FestivalAlias"
    )

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {

            add(getDebugItem("哀悼日全局置灰", View.OnClickListener {
                Config.writeConfig(
                    Constants.CONFIG_KEY_LAYER_START_VALUE, System.currentTimeMillis() / 1000 - 3600
                )
                Config.writeConfig(
                    Constants.CONFIG_KEY_LAYER_END_VALUE, System.currentTimeMillis() / 1000 + 3600
                )
                ZixieContext.restartApp()
            }))

            add(getDebugItem("哀悼日解除置灰", View.OnClickListener {
                Config.writeConfig(Constants.CONFIG_KEY_LAYER_START_VALUE, 0)
                Config.writeConfig(Constants.CONFIG_KEY_LAYER_END_VALUE, 0)
                ZixieContext.restartApp()
            }))

            add(getDebugItem("切换桌面图标1", View.OnClickListener {
                APPIconManager.changeAppIcon(
                    it.context,
                    "com.bihe0832.android.test.DefaultAlias",
                    list,2000
                )
            }))

            add(getDebugItem("切换桌面图标2", View.OnClickListener {
                APPIconManager.changeAppIcon(
                    it.context,
                    "com.bihe0832.android.test.FestivalAlias",
                    list,2000
                )
            }))

            add(getDebugItem("一键换肤", View.OnClickListener {
                startActivityWithException(ThemeActivity::class.java)
            }))


            add(getDebugItem("应用前后台信息", View.OnClickListener { testAPPObserver() }))
            add(getDebugItem("多语言测试") { showLanguageInfo() })
            add(getDebugItem("设置语言为中文") { changeToZH() })
            add(getDebugItem("设置语言为英文") { changeToEN() })
            add(getDebugItem("多语言切换") { RouterHelper.openPageByRouter(RouterConstants.MODULE_NAME_LANGUAGE) })
            add(getTipsItem(context!!.resources.getString(R.string.debug_msg)))
            add(getTipsItem(resources.getString(R.string.debug_msg)))
            add(getTipsItem(ZixieContext.applicationContext!!.resources.getString(R.string.debug_msg)))
        }
    }

    private fun testAPPObserver() {
        ZLog.d("testAPPObserver", "getAPPStartTime ： ${ApplicationObserver.getAPPStartTime()}")
        ZLog.d("testAPPObserver", "getLastPauseTime ： ${ApplicationObserver.getLastPauseTime()}")
        ZLog.d(
            "testAPPObserver", "getLastResumedTime ： ${ApplicationObserver.getLastResumedTime()}"
        )
        ZLog.d("testAPPObserver", "getCurrentActivity ： ${ActivityObserver.getCurrentActivity()}")
        ActivityObserver.getActivityList().forEach {
            ZLog.d(
                "testAPPObserver",
                "getCurrentActivity ： ${it.javaClass.name} - ${it.hashCode()} - ${it.taskId}"
            )
        }
    }

    private fun changeToZH() {
        MultiLanguageHelper.setLanguageConfig(context!!, Locale.CHINESE)
        ZixieContext.updateApplicationContext(context!!, true)
        activity!!.recreate()
    }

    private fun changeToEN() {
        MultiLanguageHelper.setLanguageConfig(context!!, Locale.US)
        ZixieContext.updateApplicationContext(context!!, true)
        activity!!.recreate()
    }

    private fun showLanguageInfo() {
        showInfo("引用当前多语言设置", mutableListOf<String>().apply {
            add("系统当前语言: ${MultiLanguageHelper.getSystemLocale().displayName}")
            add("应用当前语音: ${MultiLanguageHelper.getContextLocale(context!!).displayName}")
            add("应用设置语音: ${MultiLanguageHelper.getLanguageConfig(context!!).displayName}")
            add("页面Context: ${context!!.resources.getString(R.string.debug_msg)}")
            add(
                "页面Context实时: ${
                    MultiLanguageHelper.getRealResources(context!!).getString(R.string.debug_msg)
                }"
            )
            add("Application Context: ${ZixieContext.applicationContext!!.resources.getString(R.string.debug_msg)}")
            add(
                "Application Context 实时: ${
                    MultiLanguageHelper.getRealResources(ZixieContext.applicationContext!!)
                        .getString(R.string.debug_msg)
                }"
            )
        })
    }

    override fun onLocaleChanged(lastLocale: Locale, toLanguageTag: Locale) {
        super.onLocaleChanged(lastLocale, toLanguageTag)
        if(isRootViewCreated()){
            mDataLiveData.initData()
        }
    }
}