/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.ui


import android.view.View
import com.bihe0832.android.base.debug.theme.ThemeActivity
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.constant.Constants
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.lifecycle.ActivityObserver
import com.bihe0832.android.lib.lifecycle.ApplicationObserver
import com.bihe0832.android.lib.log.ZLog


class DebugUIFragment : DebugEnvFragment() {
    val LOG_TAG = this.javaClass.simpleName

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {

            add(getDebugItem("哀悼日全局置灰", View.OnClickListener {
                Config.writeConfig(Constants.CONFIG_KEY_LAYER_START_VALUE, System.currentTimeMillis() / 1000 - 3600)
                Config.writeConfig(Constants.CONFIG_KEY_LAYER_END_VALUE, System.currentTimeMillis() / 1000 + 3600)
                ZixieContext.restartApp()
            }))

            add(getDebugItem("哀悼日解除置灰", View.OnClickListener {
                Config.writeConfig(Constants.CONFIG_KEY_LAYER_START_VALUE, 0)
                Config.writeConfig(Constants.CONFIG_KEY_LAYER_END_VALUE, 0)
                ZixieContext.restartApp()
            }))

            add(getDebugItem("一键换肤", View.OnClickListener {
                startActivityWithException(ThemeActivity::class.java)
            }))


            add(getDebugItem("应用前后台信息", View.OnClickListener { testAPPObserver() }))

        }
    }

    private fun testAPPObserver() {
        ZLog.d("testAPPObserver", "getAPPStartTime ： ${ApplicationObserver.getAPPStartTime()}")
        ZLog.d("testAPPObserver", "getLastPauseTime ： ${ApplicationObserver.getLastPauseTime()}")
        ZLog.d("testAPPObserver", "getLastResumedTime ： ${ApplicationObserver.getLastResumedTime()}")
        ZLog.d("testAPPObserver", "getCurrentActivity ： ${ActivityObserver.getCurrentActivity()}")
        ActivityObserver.getActivityList().forEach {
            ZLog.d("testAPPObserver", "getCurrentActivity ： ${it.javaClass.name} - ${it.hashCode()} - ${it.taskId}")
        }

    }
}