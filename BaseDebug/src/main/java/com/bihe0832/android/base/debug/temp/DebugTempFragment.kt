/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.temp

import android.view.View
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.http.common.HTTPServer
import com.bihe0832.android.lib.http.common.core.BaseConnection
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.encrypt.messagedigest.MD5

class DebugTempFragment : DebugEnvFragment() {
    val LOG_TAG = this.javaClass.simpleName
    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getDebugItem("通用测试预处理", View.OnClickListener { preTest(it) }))
            add(getDebugItem("简单测试函数", View.OnClickListener { testFunc(it) }))

        }
    }


    private fun preTest(itemView: View) {

    }

    private fun testFunc(itemView: View) {

    }
}

