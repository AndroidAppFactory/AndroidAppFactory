/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.ipc


import android.view.View
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.lib.adapter.CardBaseModule


class AAFDebugIPCFragment : DebugEnvFragment() {
    val LOG_TAG = this.javaClass.simpleName

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {

            add(
                    getDebugItem(
                            "多进程",
                            View.OnClickListener { startActivityWithException(TestIPCActivity::class.java) })
            )
            add(
                    getDebugItem(
                            "多进程1",
                            View.OnClickListener { startActivityWithException(TestIPC1Activity::class.java) })
            )
        }
    }

}