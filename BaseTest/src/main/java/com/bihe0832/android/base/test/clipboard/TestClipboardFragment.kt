package com.bihe0832.android.base.test.clipboard

import android.view.View
import com.bihe0832.android.common.test.base.BaseTestFragment
import com.bihe0832.android.common.test.item.TestItemData
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.text.ClipboardUtil
import java.util.*

class TestClipboardFragment : BaseTestFragment() {
    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(TestItemData("复制到剪切板", View.OnClickListener {
                ClipboardUtil.copyToClipboard(context, "this is a test")
            }))
            add(TestItemData("读取剪切板数据", View.OnClickListener {
                ZixieContext.showToast(ClipboardUtil.pasteFromClipboard(context))
            }))
            add(TestItemData("清空剪切板", View.OnClickListener {
                ClipboardUtil.clearClipboard(context)
            }))
        }
    }
}