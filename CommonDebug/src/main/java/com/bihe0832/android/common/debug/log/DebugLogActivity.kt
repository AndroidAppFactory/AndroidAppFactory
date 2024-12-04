package com.bihe0832.android.common.debug.log;

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.item.getLittleDebugItem
import com.bihe0832.android.common.debug.log.core.DebugLogInfoActivity
import com.bihe0832.android.common.list.CardItemForCommonList
import com.bihe0832.android.common.list.CommonListLiveData
import com.bihe0832.android.common.list.swiperefresh.CommonListActivity
import com.bihe0832.android.common.webview.core.WebViewLoggerFile
import com.bihe0832.android.framework.R
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.log.LoggerFile
import com.bihe0832.android.framework.router.RouterInterrupt
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.select.FileSelectTools
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.ui.recycleview.ext.SafeGridLayoutManager
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment


open class DebugLogActivity : CommonListActivity() {
    val mDataList = ArrayList<CardBaseModule>()
    private var isView = true
    override fun getLayoutManagerForList(): RecyclerView.LayoutManager {
        return SafeGridLayoutManager(this, 3)
    }

    override fun getNavigationBarColor(): Int {
        return ThemeResourcesManager.getColor(R.color.transparent)!!
    }

    override fun getCardList(): List<CardItemForCommonList>? {
        return mutableListOf<CardItemForCommonList>().apply {
            add(CardItemForCommonList(SectionDataHeader::class.java, true))
            add(CardItemForCommonList(SectionDataContent::class.java))
            add(CardItemForCommonList(DebugItemData::class.java, true))
        }
    }

    protected fun getCommonLogList(): List<CardBaseModule> {
        return mutableListOf<CardBaseModule>().apply {
            add(SectionDataHeader("通用日志工具"))
            add(getPathItem(LoggerFile.getZixieFileLogPathByModule("*")))
            add(getSentItem())
            add(getSentItem())
            add(getViewItem())
        }
    }

    protected fun getPathItem(path: String): DebugItemData {
        return getLittleDebugItem(
            "日志路径：<BR><small>${path}</small>",
            null,
            false,
            null
        )
    }

    protected fun getSentItem(): DebugItemData {
        return getDebugItem("选择并发送日志") {
            isView = false
            FileSelectTools.openFileSelect(this@DebugLogActivity, ZixieContext.getLogFolder())
        }
    }

    protected fun getViewItem(): CardBaseModule {
        return getDebugItem("选择并查看日志") {
            isView = true
            FileSelectTools.openFileSelect(this@DebugLogActivity, ZixieContext.getLogFolder())
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
            addAll(getCommonLogList())
            add(SectionDataHeader("基础通用日志"))
            add(SectionDataContent("路由跳转", RouterInterrupt.getRouterLogPath(), true, false))
            add(SectionDataContent("Webview", WebViewLoggerFile.getWebviewLogPath(), false, true))
        }
    }

    override fun getTitleText(): String {
        return "日志功能汇总"
    }

    override fun initView() {
        super.initView()
        updateIcon(R.drawable.ic_left_arrow, true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FileSelectTools.FILE_CHOOSER && resultCode == SwipeBackFragment.RESULT_OK) {
            data?.extras?.getString(FileSelectTools.INTENT_EXTRA_KEY_WEB_URL, "")?.let { filePath ->
                if (isView) {
                    DebugLogInfoActivity.showLog(
                        this, filePath, sort = false, showLine = true, showNum = 2000
                    )
                } else {
                    FileUtils.sendFile(this@DebugLogActivity, filePath).let {
                        if (!it) {
                            ZixieContext.showToast("分享文件:$filePath 失败")
                        }
                    }
                }
            }
        }
    }
}