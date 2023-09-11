package com.bihe0832.android.base.debug.view

import android.view.View
import com.bihe0832.android.base.debug.touch.TouchRegionActivity
import com.bihe0832.android.base.debug.view.customview.DebugCustomViewFragment
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.debug.DebugTools
import com.bihe0832.android.lib.ui.dialog.callback.DialogCompletedStringCallback

class DebugBaseViewFragment : DebugEnvFragment() {
    val LOG_TAG = this.javaClass.simpleName

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getDebugFragmentItemData("特殊TextView 调试", DebugTextViewFragment::class.java))
            add(getDebugFragmentItemData("自定义View 调试", DebugCustomViewFragment::class.java))

            add(
                DebugItemData(
                    "TextView对HTML的支持测试",
                    View.OnClickListener {
                        showInputDialog(
                            "TextView对HTML的支持测试",
                            "请在输入框输入需要验证的文本内容，无需特殊编码",
                            "<font color='#428bca'>测试文字加粗</font> <BR> 正常的文字效果<BR> <b>测试文字加粗</b> <em>文字斜体</em> <p><font color='#428bca'>修改文字颜色</font></p>",
                            object :
                                DialogCompletedStringCallback {
                                override fun onResult(result: String?) {
                                    DebugTools.showInfoWithHTML(
                                        context,
                                        "TextView对HTML的支持测试",
                                        result,
                                        "分享给我们",
                                    )
                                }
                            },
                        )
                    },
                ),
            )

            add(
                DebugItemData(
                    "点击区扩大Demo",
                    View.OnClickListener {
                        startActivityWithException(TouchRegionActivity::class.java)
                    },
                ),
            )
        }
    }
}
