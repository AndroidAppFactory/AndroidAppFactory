package com.bihe0832.android.test.module

import android.content.res.Configuration
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.bihe0832.android.app.log.AAFLoggerFile
import com.bihe0832.android.common.debug.log.DebugLogListActivity
import com.bihe0832.android.common.debug.log.SectionDataContent
import com.bihe0832.android.common.debug.log.SectionDataHeader
import com.bihe0832.android.common.webview.core.WebViewLoggerFile
import com.bihe0832.android.framework.file.AAFFileTools
import com.bihe0832.android.framework.router.RouterInterrupt
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.ui.recycleview.ext.SafeGridLayoutManager

class AAFDebugLogListActivity : DebugLogListActivity() {

    override fun parseBundle(bundle: Bundle) {
        super.parseBundle(bundle)
    }
    override fun getLayoutManagerForList(): RecyclerView.LayoutManager {
        return SafeGridLayoutManager(this, 6)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        newConfig.orientation
    }

    override fun getTempData(): List<CardBaseModule> {
        return mutableListOf<CardBaseModule>().apply {
            addAll(getCommonLogList())
            add(SectionDataHeader("基础通用日志"))
            add(SectionDataContent("路由跳转路由跳转路由跳转路由跳转路由跳转路由跳转", RouterInterrupt.getRouterLogPath(), false))
            add(SectionDataContent("路由跳转", RouterInterrupt.getRouterLogPath(), false))
            add(SectionDataContent("路由跳转", RouterInterrupt.getRouterLogPath(), false))
            add(SectionDataContent("路由跳转", RouterInterrupt.getRouterLogPath(), false))
            add(SectionDataContent("路由跳转", RouterInterrupt.getRouterLogPath(), true))
            add(SectionDataContent("Webview", WebViewLoggerFile.getWebviewLogPath(), true))
            add(SectionDataContent("应用更新") { _, type ->
                try {
                    AAFLoggerFile.getLogPathByModuleName(AAFLoggerFile.MODULE_UPDATE).let {
                        if (type == SectionDataContent.TYPE_OPEN) {
//                            FileContentInfoActivity.showLog(
//                                this@AAFDebugLogListActivity,
//                                it,
//                                sort = false,
//                                showLine = true
//                            )
                            AAFFileTools.openFileWithTips(this@AAFDebugLogListActivity, it)
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