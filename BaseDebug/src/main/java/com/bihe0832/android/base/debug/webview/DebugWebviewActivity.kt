package com.bihe0832.android.base.debug.webview

import android.text.TextUtils
import android.view.View
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.ui.dialog.input.InputDialogCompletedCallback
import com.bihe0832.android.lib.router.annotation.Module
import com.bihe0832.android.common.debug.base.BaseDebugListActivity
import com.bihe0832.android.common.debug.item.DebugItemData

const val ROUTRT_NAME_TEST_WEBVIEW = "testweb"

@Module(ROUTRT_NAME_TEST_WEBVIEW)
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

        }
    }

    override fun initView() {
        super.initView()
        mToolbar?.visibility = View.GONE
    }

    override fun getTitleText(): String {
        return "Webviewl测试"
    }
}