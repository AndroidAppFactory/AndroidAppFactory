package com.bihe0832.android.common.debug.log;

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.bihe0832.android.common.file.preview.ContentItemData
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.item.getLittleDebugItem
import com.bihe0832.android.common.list.CardItemForCommonList
import com.bihe0832.android.common.list.CommonListLiveData
import com.bihe0832.android.common.list.swiperefresh.CommonListActivity
import com.bihe0832.android.common.webview.core.WebViewLoggerFile
import com.bihe0832.android.lib.aaf.res.R as ResR
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.log.LoggerFile
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.framework.router.RouterInterrupt
import com.bihe0832.android.framework.router.showFileContent
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.select.FileSelectTools
import com.bihe0832.android.lib.router.annotation.Module
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.ui.recycleview.ext.SafeGridLayoutManager
import com.bihe0832.android.lib.utils.ConvertUtils
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment

open class DebugLogListActivity : CommonListActivity() {

    val mDataList = ArrayList<CardBaseModule>()
    private var isView = true
    private var showTitle = false

    override fun parseBundle(bundle: Bundle) {
        super.parseBundle(bundle)
        showTitle = ConvertUtils.parseBoolean(
            bundle.getString(
                RouterConstants.INTENT_EXTRA_KEY_SHOW_LOG_LIST_ACTION,
                ""
            ), showTitle
        )
    }

    open fun showAction(): Boolean {
        return false
    }

    override fun getLayoutManagerForList(): RecyclerView.LayoutManager {
        return SafeGridLayoutManager(this, 3)
    }

    override fun getNavigationBarColor(): Int {
        return ThemeResourcesManager.getColor(ResR.color.transparent)!!
    }

    override fun getCardList(): List<CardItemForCommonList>? {
        return mutableListOf<CardItemForCommonList>().apply {
            add(CardItemForCommonList(SectionDataHeader::class.java, true))
            add(CardItemForCommonList(SectionDataContent::class.java))
            add(CardItemForCommonList(ContentItemData::class.java, true))
        }
    }

    protected open fun getCommonLogList(): List<CardBaseModule> {
        return mutableListOf<CardBaseModule>().apply {
            add(SectionDataHeader("通用日志工具"))
            add(getLogPathItem(LoggerFile.getZixieFileLogPathByModule("*")))
            add(getSendLogItem(ZixieContext.getLogFolder()))
            add(getOpenLogItem(ZixieContext.getLogFolder()))
        }
    }

    protected open fun getItemList(): List<CardBaseModule> {
        return mutableListOf<CardBaseModule>().apply {
            add(SectionDataHeader("基础通用日志"))
            add(
                SectionDataContent(
                    "路由跳转", RouterInterrupt.getRouterLogPath(), showAction(), true, false
                )
            )
            add(
                SectionDataContent(
                    "Webview", WebViewLoggerFile.getWebviewLogPath(), showAction(), false, true
                )
            )
        }
    }

    protected fun getLogPathItem(path: String): ContentItemData {
        return getLittleDebugItem(
            "日志路径：<BR><small>${path}</small>", null, false, null
        )
    }

    protected fun getSendLogItem(path: String): ContentItemData {
        return getDebugItem("选择并发送日志") {
            isView = false
            FileSelectTools.openFileSelect(this@DebugLogListActivity, path)
        }
    }

    protected fun getOpenLogItem(path: String): CardBaseModule {
        return getDebugItem("选择并查看日志") {
            isView = true
            FileSelectTools.openFileSelect(this@DebugLogListActivity, path)
        }
    }

    override fun getDataLiveData(): CommonListLiveData {
        return object : CommonListLiveData() {
            override fun initData() {
                mDataList.addAll(getTempData())
                postValue(mDataList)
            }

            override fun refresh() {
            }

            override fun loadMore() {
            }

            override fun hasMore(): Boolean {
                return false
            }

            override fun canRefresh(): Boolean {
                return false
            }
        }
    }

    open fun getTempData(): List<CardBaseModule> {
        return mutableListOf<CardBaseModule>().apply {
            if (showTitle) {
                addAll(getCommonLogList())
            }
            addAll(getItemList())
        }
    }

    override fun getTitleText(): String {
        return "日志功能汇总"
    }

    override fun initView() {
        super.initView()
        updateIcon(ResR.drawable.icon_left_arrow, true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FileSelectTools.FILE_CHOOSER && resultCode == SwipeBackFragment.RESULT_OK) {
            data?.extras?.getString(FileSelectTools.INTENT_EXTRA_KEY_WEB_URL, "")?.let { filePath ->
                if (isView) {
                    showFileContent(filePath, isReversed = false, showLine = true, showNum = 2000)
                } else {
                    FileUtils.sendFile(this@DebugLogListActivity, filePath).let {
                        if (!it) {
                            ZixieContext.showToast("分享文件:$filePath 失败")
                        }
                    }
                }
            }
        }
    }
}