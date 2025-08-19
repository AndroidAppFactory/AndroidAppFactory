package com.bihe0832.android.base.debug.share

import android.view.View
import com.bihe0832.android.base.debug.dialog.DebugBottomActivity
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.common.share.ShareAPPActivity
import com.bihe0832.android.lib.adapter.CardBaseModule

class DebugShareFragment : DebugEnvFragment() {


    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getDebugItem("分享APK", View.OnClickListener { startActivityWithException(ShareAPPActivity::class.java) }))
            add(getDebugItem("底部分享Activity", View.OnClickListener { startActivityWithException(
                DebugBottomActivity::class.java) }))
        }
    }
}