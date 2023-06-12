/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.temp


import android.graphics.BitmapFactory
import android.view.View
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.media.image.BitmapUtil
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.os.DisplayUtil
import java.net.URL


class DebugTempFragment : DebugEnvFragment() {
    val LOG_TAG = this.javaClass.simpleName
    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugItemData("简单测试函数", View.OnClickListener { testFunc() }))
            add(DebugItemData("通用测试预处理", View.OnClickListener { preTest(it) }))
        }
    }


    private fun preTest(itemView: View) {

    }

    private fun testFunc() {
        val imgUrl = "https://cdn.bihe0832.com/images/head.jpg"
        ThreadManager.getInstance().start {
            var a = BitmapUtil.getRemoteBitmap(imgUrl, DisplayUtil.dip2px(context, 40f), DisplayUtil.dip2px(context, 40f))
            val url = URL(imgUrl)
            val ist = url.openStream()
            BitmapFactory.decodeStream(ist).let {
                ZixieContext.showToast(it.toString())
            }
        }
    }
}