package com.bihe0832.android.base.debug.webview

import android.text.TextUtils
import android.view.View
import com.bihe0832.android.common.debug.base.BaseDebugListActivity
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.ui.dialog.input.InputDialogCompletedCallback

class DebugWebviewActivity : BaseDebugListActivity() {

    private var lastUrl = "https://blog.bihe0832.com"

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugItemData("打开指定Web页面", View.OnClickListener {
                showInputDialog("打开指定Web页面", "请在输入框输入网页地址后点击“确定”", lastUrl, InputDialogCompletedCallback { result: String ->
                    try {
                        if (!TextUtils.isEmpty(result)) {
                            lastUrl = result
                            openWeb(result)
                        } else {
                            ZixieContext.showDebug("请输入正确的网页地址")
                        }
                    } catch (e: Exception) {
                    }
                })
            }))
            add(DebugItemData("打开JSbridge调试页面", View.OnClickListener { openWeb("https://microdemo.bihe0832.com/jsbridge/index.html") }))
            add(DebugItemData("打开TBS调试页面", View.OnClickListener { openWeb("http://debugtbs.qq.com/") }))
            add(DebugItemData("打开本地调试页", View.OnClickListener { openWeb("file:///android_asset/index.html") }))
        }
    }

    override fun getTitleText(): String {
        return "Webview调试"
    }
}