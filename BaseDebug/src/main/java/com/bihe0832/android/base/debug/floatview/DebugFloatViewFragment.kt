/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.floatview


import android.view.View
import com.bihe0832.android.base.debug.floatview.icon.DebugIcon
import com.bihe0832.android.base.debug.floatview.icon.DebugTipsIcon
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.debug.icon.DebugLogTips
import com.bihe0832.android.lib.floatview.IconManager


class DebugFloatViewFragment : DebugEnvFragment() {
    val LOG_TAG = "DebugTempFragment"

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugItemData("展示悬浮窗", View.OnClickListener { showIcon() }))
            add(DebugItemData("隐藏悬浮窗", View.OnClickListener { hideIcon() }))
        }
    }


    val mIcon by lazy {
        DebugIcon(activity)
    }
    val mIconManager by lazy {
        IconManager(activity!!, mIcon).apply {
            setIconClickListener(View.OnClickListener {
                ZixieContext.showToast("点了一下Icon")
            })
        }
    }

    val mDebugTips by lazy {
        DebugTipsIcon(activity!!)
    }



    private fun showIcon() {
        mIconManager.showIconWithPermissionCheck(null)
        mDebugTips.append("<B>提示信息</B>:<BR>    ")
        DebugLogTips.append("<B>提示信息</B> fs df d fsdf:     ")
        mIcon.setHasNew(true)
    }

    private fun hideIcon() {
        mIconManager.hideIcon()
        mDebugTips.show("")
        DebugLogTips.show("")
    }

}