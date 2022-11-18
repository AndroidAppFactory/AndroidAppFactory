/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.cache


import android.view.View
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.lib.adapter.CardBaseModule


class DebugCacheFragment : DebugEnvFragment() {

    var num = 0
    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugItemData("测试数据缓存效果", View.OnClickListener {
                DebugInfoCacheManager.loggerData()
            }))

            add(DebugItemData("测试数据丢弃", View.OnClickListener {
                for (i in 0..5) {
                    num++
                    DebugInfoCacheManager.addData("TestCache$num", DebugCacheData().apply {
                        this.key = "TestCache$num"
                    })
                }
            }))
        }
    }
}