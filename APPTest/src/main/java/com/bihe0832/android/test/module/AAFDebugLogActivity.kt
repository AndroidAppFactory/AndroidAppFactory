package com.bihe0832.android.test.module

import androidx.recyclerview.widget.RecyclerView
import com.bihe0832.android.app.log.AAFLoggerFile
import com.bihe0832.android.common.debug.log.DebugLogActivity
import com.bihe0832.android.common.debug.log.SectionDataContent
import com.bihe0832.android.common.debug.log.SectionDataHeader
import com.bihe0832.android.common.webview.core.WebViewLoggerFile
import com.bihe0832.android.framework.file.AAFFileTools
import com.bihe0832.android.framework.router.RouterInterrupt
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.ui.recycleview.ext.SafeGridLayoutManager

class AAFDebugLogActivity : DebugLogActivity() {
    override fun getLayoutManagerForList(): RecyclerView.LayoutManager {
        return SafeGridLayoutManager(this, 3)
    }

    override fun getTempData(): List<CardBaseModule> {
        return mutableListOf<CardBaseModule>().apply {
            addAll(getCommonLogList())
            add(SectionDataHeader("基础通用日志"))
            add(SectionDataContent("路由跳转", RouterInterrupt.getRouterLogPath()))
            add(SectionDataContent("Webview", WebViewLoggerFile.getWebviewLogPath()))
            add(SectionDataContent("应用更新") { _, type ->
                try {
                    AAFLoggerFile.getLogPathByModuleName(AAFLoggerFile.MODULE_UPDATE).let {
                        if (type == SectionDataContent.TYPE_OPEN) {
                            AAFFileTools.openFileWithTips(this@AAFDebugLogActivity, it)
                        } else {
                            AAFFileTools.sendFile(it)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            })
        }
    }

}