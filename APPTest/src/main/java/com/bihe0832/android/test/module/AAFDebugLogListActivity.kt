package com.bihe0832.android.test.module

import com.bihe0832.android.app.log.AAFLoggerFile
import com.bihe0832.android.common.compose.debug.log.DebugLogComposeActivity
import com.bihe0832.android.common.compose.debug.log.item.ItemOnClickListener
import com.bihe0832.android.common.webview.core.WebViewLoggerFile
import com.bihe0832.android.framework.file.AAFFileTools
import com.bihe0832.android.framework.router.RouterInterrupt

class AAFDebugLogListActivity : DebugLogComposeActivity() {


    override fun getLogList(): List<LogInfo> {
        return mutableListOf(
            LogInfo("路由跳转", RouterInterrupt.getRouterLogPath()),
            LogInfo("Webview", WebViewLoggerFile.getWebviewLogPath(), showAction = false),
            LogInfo(
                "应用更新",
                AAFLoggerFile.getLogPathByModuleName(AAFLoggerFile.MODULE_UPDATE),
                clickAction = object : ItemOnClickListener {
                    override fun onClick(title: String, path: String, type: Int) {
                        try {
                            if (type == ItemOnClickListener.TYPE_OPEN) {
                                AAFFileTools.openFileWithTips(this@AAFDebugLogListActivity, path)
                            } else {
                                AAFFileTools.sendFile(path)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                })
        )
    }

}