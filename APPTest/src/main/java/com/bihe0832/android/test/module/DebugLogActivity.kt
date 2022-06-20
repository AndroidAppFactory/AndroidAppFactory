package com.bihe0832.android.test.module

import android.support.v7.widget.RecyclerView
import com.bihe0832.android.app.log.AAFLoggerFile
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.log.DebugLogActivity
import com.bihe0832.android.common.debug.log.SectionDataContent
import com.bihe0832.android.common.debug.log.SectionDataHeader
import com.bihe0832.android.common.webview.log.WebviewLoggerFile
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.log.LoggerFile
import com.bihe0832.android.framework.router.RouterInterrupt
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.file.select.FileSelectTools
import com.bihe0832.android.lib.ui.recycleview.ext.SafeGridLayoutManager

class DebugLogActivity : DebugLogActivity() {
    override fun getLayoutManagerForList(): RecyclerView.LayoutManager {
        return SafeGridLayoutManager(this, 2)
    }

    override fun getTempData(): List<CardBaseModule> {
        return mutableListOf<CardBaseModule>().apply {
            add(SectionDataHeader("通用日志工具"))
            add(DebugItemData("选择并发送单个日志") { FileSelectTools.openFileSelect(this@DebugLogActivity, ZixieContext.getLogFolder()) })
            add(DebugItemData("上传日志") { })
            add(SectionDataHeader("基础通用日志"))
            add(SectionDataContent("路由跳转", RouterInterrupt.getRouterLogPath()))
            add(SectionDataContent("Webview", WebviewLoggerFile.getWebviewLogPath()))
            add(SectionDataContent("应用更新") { _, type ->
                try {
                    AAFLoggerFile.getLogPathByModuleName(AAFLoggerFile.MODULE_UPDATE).let {
                        if (type == SectionDataContent.TYPE_OPEN) {
                            LoggerFile.openLog(it)
                        } else {
                            LoggerFile.sendLog(it)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            })
        }
    }

}