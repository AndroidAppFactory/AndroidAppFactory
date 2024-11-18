package com.bihe0832.android.base.debug.webview

import android.content.Intent
import android.text.TextUtils
import android.view.View
import com.bihe0832.android.common.debug.base.BaseDebugListFragment
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.router.RouterAction
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.framework.router.openZixieWeb
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.ui.dialog.callback.DialogCompletedStringCallback

class DebugWebViewFragment : BaseDebugListFragment() {

    private var lastUrl = "https://blog.bihe0832.com"

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugItemData("打开指定Web页面", View.OnClickListener {
                showInputDialog("打开指定Web页面", "请在输入框输入网页地址后点击“确定”", lastUrl,
                    DialogCompletedStringCallback { result: String ->
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
            add(DebugItemData("原生内核打开JSbridge调试页面", View.OnClickListener { openWeb("https://microdemo.bihe0832.com/jsbridge/index.html") }))
            add(DebugItemData("原生内核打开TBS调试页面", View.OnClickListener { openWeb("http://debugtbs.qq.com/") }))
            add(DebugItemData("原生内核打开本地调试页", View.OnClickListener { openWeb("file:///android_asset/index.html") }))
            add(DebugItemData("X5内核打开JSbridge调试页面", View.OnClickListener { openTBSWeb("https://microdemo.bihe0832.com/jsbridge/index.html") }))
            add(DebugItemData("X5内核打开TBS调试页面", View.OnClickListener { openTBSWeb("http://debugtbs.qq.com/") }))
            add(DebugItemData("X5内核打开本地调试页", View.OnClickListener { openTBSWeb("file:///android_asset/index.html") }))
            add(DebugItemData("H5与原生混排Demo", View.OnClickListener { startDebugActivity(DebugH5NativeWebFragment::class.java) }))
            add(DebugItemData("H5与原生混排实践", View.OnClickListener { startDebugActivity(DebugWebViewWithNativeFragment::class.java) }))

        }
    }


    fun openTBSWeb(url: String) {
        val map = HashMap<String, String>()
        map[RouterConstants.INTENT_EXTRA_KEY_WEB_URL] = URLUtils.encode(url)
        RouterAction.openFinalURL(RouterAction.getFinalURL(RouterConstants.MODULE_NAME_WEB_PAGE_TBS, map), Intent.FLAG_ACTIVITY_NEW_TASK)
    }


    fun openWeb(url: String) {
        openZixieWeb(url)
    }
}