/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.temp

import android.os.Handler
import android.view.View
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.thread.ThreadManager

class DebugTempFragment : DebugEnvFragment() {
    val LOG_TAG = this.javaClass.simpleName
    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getDebugItem("通用测试预处理", View.OnClickListener { preTest(it) }))
            add(getDebugItem("简单测试函数", View.OnClickListener { testFunc(it) }))

        }
    }

    private val mHandler =
        Handler(ThreadManager.getInstance().getLooper(ThreadManager.LOOPER_TYPE_ANDROID_MAIN))

    private fun preTest(itemView: View) {
        mHandler.postDelayed({
            Thread.sleep(10 * 1000)
        }, 0)
    }

    private fun testFunc(itemView: View) {
        mHandler.postDelayed({
            Thread.sleep(5 * 1000)
        }, 0)
        Thread.sleep(5 * 1000)
    }
}

