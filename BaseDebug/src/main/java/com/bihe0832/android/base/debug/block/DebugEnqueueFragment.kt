/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.block


import android.view.View
import com.bihe0832.android.lib.block.task.BlockTaskManager
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.lib.adapter.CardBaseModule


class DebugEnqueueFragment : DebugEnvFragment() {
    val LOG_TAG = "DebugEnqueueFragment"

    var num = 0

    private val mTaskQueue = BlockTaskManager()
    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {

            add(DebugItemData("添加中优先级任务", View.OnClickListener {
                num++
                mTaskQueue.add(LogTask("DEFAULT：$num").apply {
                    setPriority(0)
                })

            }))
            add(DebugItemData("添加低优先级任务", View.OnClickListener {
                num++
                mTaskQueue.add(LogTask("LOW：$num").apply {
                    setPriority(-1)
                })

            }))
            add(DebugItemData("添加高优先级任务", View.OnClickListener {
                num++
                mTaskQueue.add(LogTask("HIGH：$num").apply {
                    setPriority(5)
                })

            }))
            add(DebugItemData("添加递增高优先级任务", View.OnClickListener {
                num++
                mTaskQueue.add(LogTask("HIGH：$num").apply {
                    setPriority(num)
                })

            }))
        }
    }
}