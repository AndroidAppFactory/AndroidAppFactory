package com.bihe0832.android.test.module

import android.support.v7.widget.RecyclerView
import com.bihe0832.android.app.log.AAFLoggerFile
import com.bihe0832.android.common.test.item.TestItemData
import com.bihe0832.android.common.test.log.SectionDataContent
import com.bihe0832.android.common.test.log.SectionDataHeader
import com.bihe0832.android.common.test.log.TestLogActivity
import com.bihe0832.android.common.webview.log.WebviewLoggerFile
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.log.LoggerFile
import com.bihe0832.android.framework.router.RouterInterrupt
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.file.select.FileSelectTools
import com.bihe0832.android.lib.ui.recycleview.ext.SafeGridLayoutManager

class TestLogActivity : TestLogActivity() {
    override fun getLayoutManagerForList(): RecyclerView.LayoutManager {
        return SafeGridLayoutManager(this, 2)
    }

    override fun getTempData(): List<CardBaseModule> {
        return mutableListOf<CardBaseModule>().apply {
            add(SectionDataHeader("通用日志工具"))
            add(TestItemData("选择并发送单个日志") { FileSelectTools.openFileSelect(this@TestLogActivity, ZixieContext.getLogFolder()) })
            add(TestItemData("上传日志") { })
            add(SectionDataHeader("基础通用日志"))
            add(SectionDataContent("路由跳转", RouterInterrupt.getRouterLogPath()))
            add(SectionDataContent("Webview", WebviewLoggerFile.getWebviewLogPath()))
            add(SectionDataContent("应用更新", AAFLoggerFile.getLogPathByModuleName(AAFLoggerFile.MODULE_UPDATE)))
        }
    }

}