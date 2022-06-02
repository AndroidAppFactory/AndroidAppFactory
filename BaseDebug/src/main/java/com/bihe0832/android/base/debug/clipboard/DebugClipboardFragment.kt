package com.bihe0832.android.base.debug.clipboard

import android.view.View
import com.bihe0832.android.common.debug.base.BaseDebugListFragment
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.text.ClipboardUtil
import java.util.*

class DebugClipboardFragment : BaseDebugListFragment() {
    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugItemData("复制到剪切板", View.OnClickListener {
                ClipboardUtil.copyToClipboard(context, "this is a test")
            }))
            add(DebugItemData("读取剪切板数据", View.OnClickListener {
                ZixieContext.showToast(ClipboardUtil.pasteFromClipboard(context))
            }))
            add(DebugItemData("清空剪切板", View.OnClickListener {
                ClipboardUtil.clearClipboard(context)
            }))
        }
    }
}