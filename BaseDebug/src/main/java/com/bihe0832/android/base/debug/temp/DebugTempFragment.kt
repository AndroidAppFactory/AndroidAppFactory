/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.temp

import android.view.View
import com.bihe0832.android.base.compose.debug.list.model.DebugPageListActivity
import com.bihe0832.android.common.compose.common.activity.CommonComposeActivity
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.lib.adapter.CardBaseModule


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
        startActivityWithException(CommonComposeActivity::class.java)
    }
}

